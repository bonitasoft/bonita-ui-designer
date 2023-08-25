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

import java.io.IOException;

import org.bonitasoft.web.designer.common.migration.Version;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.PageRepository;
import org.bonitasoft.web.designer.common.repository.WidgetRepository;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.model.ArtifactStatusReport;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.migrationReport.MigrationReport;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.service.ArtifactService;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.workspace.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/rest/migration")
public class MigrationResource {

    private final PageRepository pageRepository;
    private final WidgetRepository widgetRepository;
    private final Workspace workspace;
    private final FragmentRepository fragmentRepository;
    private final FragmentService fragmentService;

    private final PageService pageService;
    private final WidgetService widgetService;
    private final UiDesignerProperties uiDesignerProperties;

    @Autowired
    public MigrationResource(
            Workspace workspace, PageRepository pageRepository, WidgetRepository widgetRepository,
            FragmentRepository fragmentRepository, PageService pageService, WidgetService widgetService, FragmentService fragmentService, UiDesignerProperties uiDesignerProperties) {
        this.workspace = workspace;
        this.pageRepository = pageRepository;
        this.widgetRepository = widgetRepository;
        this.fragmentRepository = fragmentRepository;
        this.pageService = pageService;
        this.widgetService = widgetService;
        this.fragmentService = fragmentService;
        this.uiDesignerProperties = uiDesignerProperties;
    }

    public static ResponseEntity<MigrationReport> migrateArtifact(String artifactId, DesignerArtifact designerArtifact, ArtifactService service) {
        designerArtifact.setStatus(service.getStatus(designerArtifact));
        MigrationReport mR;

        if (designerArtifact.getArtifactVersion() != null && designerArtifact.getStatus() != null) {
            if (!designerArtifact.getStatus().isCompatible()) {
                mR = new MigrationReport(MigrationStatus.INCOMPATIBLE, designerArtifact.getId());
                mR.setComments("Artifact is incompatible with actual version");

                return new ResponseEntity<>(mR, HttpStatus.OK);

            } else if (!designerArtifact.getStatus().isMigration()) {
                mR = new MigrationReport(MigrationStatus.NONE, designerArtifact.getId());
                mR.setComments("No migration is needed");

                return new ResponseEntity<>(mR, HttpStatus.OK);
            }
        }

        try {

            MigrationResult<DesignerArtifact> migrationResult = service.migrateWithReport(designerArtifact);
            DesignerArtifact newArtifact = migrationResult.getArtifact();
            mR = new MigrationReport(migrationResult.getFinalStatus(), newArtifact.getId());
            mR.setType(newArtifact.getType());
            mR.setPreviousArtifactVersion(newArtifact.getPreviousArtifactVersion());
            mR.setMigrationStepReport(migrationResult.getMigrationStepReportListFilterByFinalStatus());
            if (migrationResult.getFinalStatus() == MigrationStatus.ERROR) {
                mR.setNewArtifactVersion(newArtifact.getPreviousArtifactVersion());

                return new ResponseEntity<>(mR, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            mR.setNewArtifactVersion(newArtifact.getArtifactVersion());

            return new ResponseEntity<>(mR, HttpStatus.OK);

        } catch (Exception e) {
            mR = new MigrationReport(MigrationStatus.ERROR, artifactId);

            return new ResponseEntity<>(mR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @ResponseBody
    public String migrate() {
        workspace.migrateWorkspace();
        return "Migration has been triggered.";
    }

    @PutMapping(value = "/page/{pageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MigrationReport> migratePage(@PathVariable("pageId") String pageId) throws RepositoryException {
        var page = pageRepository.get(pageId);
        return migrateArtifact(pageId, page, pageService);
    }

    @PutMapping(value = "/widget/{widgetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MigrationReport> migrateWidget(@PathVariable("widgetId") String widgetId) throws RepositoryException {
        var widget = widgetRepository.get(widgetId);
        return migrateArtifact(widgetId, widget, widgetService);
    }

    @GetMapping(value = "/status/page/{pageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ArtifactStatusReport statusByPageId(@PathVariable("pageId") String pageId) {
        return pageService.getStatus(pageRepository.get(pageId));
    }

    @GetMapping(value = "/status/widget/{widgetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ArtifactStatusReport statusByWidgetId(@PathVariable("widgetId") String widgetId) {
        return widgetService.getStatus(widgetRepository.get(widgetId));
    }

    @PostMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtifactStatusReport> statusByArtifactJson(
            @RequestBody String artifact) {

        var mapper = new ObjectMapper();
        JsonNode artifactNode;
        try {
            artifactNode = mapper.readTree(artifact);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        var artifactVersionNode = artifactNode.get("modelVersion");
        if (artifactVersionNode == null) {
            artifactVersionNode = artifactNode.get("designerVersion");
        }
        var currentVersion = new Version(this.uiDesignerProperties.getModelVersion());
        var artifactVersion = (artifactVersionNode != null) ? new Version(artifactVersionNode.asText()) : null;
        var artifactStatusReport = compareVersions(artifactVersion, currentVersion);
        return ResponseEntity.ok(artifactStatusReport);
    }

    @GetMapping(value = "/status/fragment/{fragmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ArtifactStatusReport statusByFragmentId(@PathVariable("fragmentId") String fragmentId) {
        var fragment = fragmentRepository.get(fragmentId);
        return fragmentService.getStatus(fragment);
    }

    @PutMapping(value = "/fragment/{fragmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MigrationReport> migrateFragments(@PathVariable("fragmentId") String fragmentId) throws RepositoryException {
        var fragment = fragmentRepository.get(fragmentId);
        return migrateArtifact(fragmentId, fragment, fragmentService);

    }

    private ArtifactStatusReport compareVersions(Version artifactVersion, Version currentVersion) {
        // Check status of this artifact
        var migration = false;
        var compatible = true;
        if (artifactVersion == null || currentVersion.isGreaterThan(artifactVersion.toString())) {
            migration = true;
        }
        if (artifactVersion != null && artifactVersion.isGreaterThan(currentVersion.toString())) {
            compatible = false;
        }

        return new ArtifactStatusReport(compatible, migration);
    }

}
