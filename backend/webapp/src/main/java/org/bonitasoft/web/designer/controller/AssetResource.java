/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.controller;

import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.asset.AssetService.OrderType;
import org.bonitasoft.web.designer.controller.importer.ServerImportException;
import org.bonitasoft.web.designer.controller.utils.HttpFile;
import org.bonitasoft.web.designer.service.AssetableArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;
import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_UPDATE;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.DECREMENT;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.INCREMENT;
import static org.bonitasoft.web.designer.controller.utils.HttpFile.getOriginalFilename;

public abstract class AssetResource<T extends Identifiable & Assetable, S extends AssetableArtifactService<T>> {

    protected static final Logger logger = LoggerFactory.getLogger(AssetResource.class);

    protected final JsonHandler jsonHandler;

    protected final S service;

    protected final Optional<SimpMessagingTemplate> messagingTemplate;

    protected AssetResource(JsonHandler jsonHandler, S service, SimpMessagingTemplate messagingTemplate) {
        this.jsonHandler = jsonHandler;
        this.service = service;
        this.messagingTemplate = ofNullable(messagingTemplate);
    }

    private void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    // produces = MediaType.TEXT_PLAIN_VALUE to avoid some internet explorer issues
    @PostMapping(value = "/{artifactId}/assets/{type}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Asset> saveOrUpdate(@RequestParam("file") MultipartFile
                                                      file, @PathVariable("artifactId") String id, @PathVariable("type") String type) {
        checkArgument(file != null && !file.isEmpty(), "Part named [file] is needed to successfully import a component");
        var assetType = AssetType.getAsset(type);
        checkArgument(assetType != null, AssetService.ASSET_TYPE_IS_REQUIRED);
        try {
            if (AssetType.JSON.equals(assetType)) {
                jsonHandler.checkValidJson(file.getBytes());
            }

            var asset = service.saveOrUpdateAsset(id, assetType, getOriginalFilename(file.getOriginalFilename()), file.getBytes());

            messagingTemplate.ifPresent(template -> template.convertAndSend(PREVIEWABLE_UPDATE, id));
            return ResponseEntity.status(HttpStatus.CREATED).body(asset);
        } catch (IOException e) {
            throw new ServerImportException("Error while uploading asset " + file.getOriginalFilename(), e);
        }
    }

    @RequestMapping("/{artifactId}/assets/{type}/{filename:.*}")
    public void downloadAsset(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("artifactId") String id,
            @PathVariable("type") String type,
            @PathVariable("filename") String filename,
            @RequestParam(value = "format", required = false) String format) throws IOException {

        var filePath = service.findAssetPath(id, filename, type);

        if ("text".equals(format)) {
            HttpFile.writeFileInResponseForVisualization(request, response, filePath);
        } else {
            HttpFile.writeFileInResponseForDownload(response, filePath);
        }
    }

    @PostMapping(value = "/{artifactId}/assets")
    public Asset saveAsset(@RequestBody Asset asset, @PathVariable("artifactId") String id) {
        return service.saveAsset(id, asset);
    }

    @DeleteMapping(value = "/{artifactId}/assets/{assetId}")
    public void deleteAsset(@PathVariable("artifactId") String id, @PathVariable("assetId") String assetId) throws
            RepositoryException {
        service.deleteAsset(id, assetId);
    }

    @PutMapping(value = "/{artifactId}/assets/{assetId}")
    public void updateAsset(
            @PathVariable("artifactId") String id,
            @PathVariable("assetId") String assetId,
            @RequestParam(value = "increment", required = false) Boolean increment,
            @RequestParam(value = "decrement", required = false) Boolean decrement,
            @RequestParam(value = "active", required = false) Boolean active) {

        // not always: update order
        if (increment != null || decrement != null) {
            final OrderType orderType = TRUE.equals(increment) ? INCREMENT : DECREMENT;
            service.changeAssetOrder(id, assetId, orderType);
        }
        // not always: update active state
        if (active != null) {
            service.changeAssetStateInPreviewable(id, assetId, active);
        }
    }
}
