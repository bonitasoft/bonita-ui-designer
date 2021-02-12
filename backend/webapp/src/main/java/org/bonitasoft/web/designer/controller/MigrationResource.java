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
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.migration.Version;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationReport;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.service.AbstractArtifactService;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.workspace.WorkspaceInitializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/migration")
public class MigrationResource {

    private final PageRepository pageRepository;
    private final WidgetRepository widgetRepository;
    private final WorkspaceInitializer workspaceInitializer;
    private final FragmentRepository fragmentRepository;
    private final FragmentService fragmentService;

    private final PageService pageService;
    private final WidgetService widgetService;
    private final UiDesignerProperties uiDesignerProperties;

    @Inject
    public MigrationResource(
            WorkspaceInitializer workspaceInitializer, PageRepository pageRepository, WidgetRepository widgetRepository,
            FragmentRepository fragmentRepository, PageService pageService, WidgetService widgetService, FragmentService fragmentService, UiDesignerProperties uiDesignerProperties) {
        this.workspaceInitializer = workspaceInitializer;
        this.pageRepository = pageRepository;
        this.widgetRepository = widgetRepository;
        this.fragmentRepository = fragmentRepository;
        this.pageService = pageService;
        this.widgetService = widgetService;
        this.fragmentService = fragmentService;
        this.uiDesignerProperties = uiDesignerProperties;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String migrate() {
        migrateAllArtifacts();
        return "Migration has been triggered.";
    }

    @RequestMapping(value = "/page/{pageId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MigrationReport> migratePage(@PathVariable("pageId") String pageId) throws RepositoryException {
        Page page = pageRepository.get(pageId);
        return migrateArtifact(pageId, page, pageService);
    }

    @RequestMapping(value = "/widget/{widgetId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MigrationReport> migrateWidget(@PathVariable("widgetId") String widgetId) throws RepositoryException {
        Widget widget = widgetRepository.get(widgetId);
        return migrateArtifact(widgetId, widget, widgetService);
    }

    public static ResponseEntity<MigrationReport> migrateArtifact(String artifactId, DesignerArtifact designerArtifact, AbstractArtifactService service) {
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

    @RequestMapping(value = "/status/page/{pageId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public MigrationStatusReport statusByPageId(@PathVariable("pageId") String pageId) {
        return pageService.getStatus(pageRepository.get(pageId));
    }

    @RequestMapping(value = "/status/widget/{widgetId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public MigrationStatusReport statusByWidgetId(@PathVariable("widgetId") String widgetId) {
        return  widgetService.getStatus(widgetRepository.get(widgetId));
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
        Version currentVersion = new Version(this.uiDesignerProperties.getModelVersion());
        Version artifactVersion = (artifactVersionNode != null) ? new Version(artifactVersionNode.asText()) : null;
        return new ResponseEntity<>(compareVersions(artifactVersion, currentVersion), HttpStatus.OK);
    }

    @RequestMapping(value = "/status/fragment/{fragmentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public MigrationStatusReport statusByFragmentId(@PathVariable("fragmentId") String fragmentId) {
        Fragment fragment = fragmentRepository.get(fragmentId);
        return fragmentService.getStatus(fragment);
    }

    @RequestMapping(value = "/fragment/{fragmentId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MigrationReport> migrateFragments(HttpServletRequest request, @PathVariable("fragmentId") String fragmentId) throws RepositoryException {
        Fragment fragment = fragmentRepository.get(fragmentId);
        return MigrationResource.migrateArtifact(fragmentId, fragment, fragmentService);

    }

    private MigrationStatusReport compareVersions(Version artifactVersion, Version currentVersion) {
        // Check status of this artifact
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


    public void migrateAllArtifacts() {
        workspaceInitializer.migrateWorkspace();
    }
}
