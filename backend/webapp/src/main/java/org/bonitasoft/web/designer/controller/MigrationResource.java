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

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.migration.Version;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.workspace.WorkspaceInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/rest/migration")
public class MigrationResource {

    private final PageRepository pageRepository;
    private final WidgetRepository widgetRepository;

    private final WorkspaceInitializer workspaceInitializer;
    @Value("${designer.modelVersion}")
    protected String modelVersion;


    @Inject
    public MigrationResource(
            WorkspaceInitializer workspaceInitializer,
            PageRepository pageRepository,
            WidgetRepository widgetRepository) {

        this.workspaceInitializer = workspaceInitializer;
        this.pageRepository = pageRepository;
        this.widgetRepository = widgetRepository;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String migrate(){

        migrateAllArtifacts();

        return "Migration has been triggered.";
    }

    @RequestMapping(value = "/status/page/{pageId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public MigrationStatusReport statusByPageId(@PathVariable("pageId") String pageId) {
        Page currentPage = pageRepository.get(pageId);
        return getStatus(currentPage);
    }

    @RequestMapping(value = "/status/widget/{widgetId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public MigrationStatusReport statusByWidgetId(@PathVariable("widgetId") String widgetId) {
        Widget currentWidget = widgetRepository.get(widgetId);
        return getStatus(currentWidget);
    }

    @RequestMapping(value = "/status", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MigrationStatusReport> statusByArtifactJson(
            @RequestBody String artifact) {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode artifactNode;
        try {
            artifactNode = mapper.readTree(artifact);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        JsonNode artifactVersionNode = artifactNode.get("modelVersion");
        if (artifactVersionNode == null) {
            artifactVersionNode = artifactNode.get("designerVersion");
        }
        Version currentVersion = new Version(modelVersion);
        Version artifactVersion = (artifactVersionNode != null) ? new Version(artifactVersionNode.asText()) : null;
        return new ResponseEntity<>(getStatus(artifactVersion, currentVersion), HttpStatus.OK);
    }

    public void migrateAllArtifacts(){
        workspaceInitializer.migrateWorkspace();
    }

    private MigrationStatusReport getStatus(DesignerArtifact artifact) {
        Version artifactVersion = new Version(artifact.getArtifactVersion());
        Version currentVersion = new Version(modelVersion);
        return getStatus(artifactVersion, currentVersion);
    }

    public static MigrationStatusReport getStatus(Version artifactVersion, Version currentVersion) {
        boolean migration = false;
        boolean compatible = true;
        if (artifactVersion == null || currentVersion.isGreaterThan(artifactVersion.toString())) {
            migration = true;
        }
        if (artifactVersion != null && artifactVersion.isGreaterThan(currentVersion.toString())) {
            compatible = false;
        }
        return new MigrationStatusReport(compatible, migration);
    }
}
