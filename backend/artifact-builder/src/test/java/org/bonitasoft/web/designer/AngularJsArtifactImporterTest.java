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
package org.bonitasoft.web.designer;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.bonitasoft.web.designer.controller.export.FragmentExporter;
import org.bonitasoft.web.designer.controller.export.PageExporter;
import org.bonitasoft.web.designer.controller.export.WidgetExporter;
import org.bonitasoft.web.designer.controller.importer.FragmentImporter;
import org.bonitasoft.web.designer.controller.importer.ImportException;
import org.bonitasoft.web.designer.controller.importer.ImportStore;
import org.bonitasoft.web.designer.controller.importer.PageImporter;
import org.bonitasoft.web.designer.controller.importer.WidgetImporter;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.bonitasoft.web.designer.workspace.Workspace;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.MODEL_NOT_FOUND;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.UNEXPECTED_ZIP_STRUCTURE;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(JUnitParamsRunner.class)
public class AngularJsArtifactImporterTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private AngularJsArtifactBuilder artifactImporter;

    private Path resources;

    @Before
    public void setUp() throws Exception {

        ImportStore importStore = new ImportStore();
        PageImporter pageImporter = mock(PageImporter.class);
        FragmentImporter fragmentImporter = mock(FragmentImporter.class);
        WidgetImporter widgetImporter = mock(WidgetImporter.class);

        Workspace workspace = mock(Workspace.class);

        PageExporter pageExporter = mock(PageExporter.class);
        FragmentExporter fragmentExporter = mock(FragmentExporter.class);
        WidgetExporter widgetExporter = mock(WidgetExporter.class);
        final HtmlGenerator htmlGenerator = mock(HtmlGenerator.class);

        artifactImporter = spy(new AngularJsArtifactBuilder(
                workspace,
                mock(WidgetService.class),
                mock(FragmentService.class),
                mock(PageService.class),
                pageExporter, fragmentExporter, widgetExporter, htmlGenerator,
                importStore, pageImporter, fragmentImporter, widgetImporter
        ));

        resources = tempDir.newFolderPath("resources");
    }

    @Test(expected = ImportException.class)
    public void should_throw_NotFoundException_for_an_unknown_artifact_type() throws Exception {
        // Given
        final boolean force = true;

        // When
        artifactImporter.importArtifact(tempDir.toPath(), force);
    }


    @Parameters({
            "page",
            "fragment",
            "widget"
    })
    @Test
    public void should_get_fragment_importer_from_type(String artifactType) throws Exception {
        // Given
        doReturn(artifactType).when(artifactImporter).resolveArtifactType(any());
        final boolean force = true;

        // When
        artifactImporter.importArtifact(tempDir.toPath(), force);

        // Then
        switch (artifactType) {
            case "page":
                verify(artifactImporter).importPage(any(), eq(force));
                break;
            case "fragment":
                verify(artifactImporter).importFragment(any(), eq(force));
                break;
            case "widget":
                verify(artifactImporter).importWidget(any(), eq(force));
                break;
            default:
                fail("unknown artifact type");
        }
    }

    @Parameters({
            "page",
            "fragment",
            "widget"
    })
    @Test
    public void should_get_importer_from_path(String artifactType) throws Exception {
        // Given
        Files.createFile(resources.resolve(artifactType + ".json"));
        final boolean force = true;

        // When
        artifactImporter.importArtifact(tempDir.toPath(), force);

        // Then
        switch (artifactType) {
            case "page":
                verify(artifactImporter).importPage(any(), eq(force));
                break;
            case "fragment":
                verify(artifactImporter).importFragment(any(), eq(force));
                break;
            case "widget":
                verify(artifactImporter).importWidget(any(), eq(force));
                break;
            default:
                fail("unknown artifact type");
        }
    }


    @Test
    public void should_get_ImportException_while_resources_folder_is_absent() throws Exception {
        Files.delete(resources);

        final ImportException importException = assertThrows(ImportException.class,
                () -> artifactImporter.importArtifact(tempDir.toPath(), true));

        assertThat(importException.getType()).isEqualTo(UNEXPECTED_ZIP_STRUCTURE);
        assertThat(importException.getMessage()).isEqualTo("Incorrect zip structure, resources folder is needed");
    }

    @Test
    public void should_get_ImportException_while_no_importer_found_from_path() throws Exception {

        final ImportException importException = assertThrows(ImportException.class, () ->
                artifactImporter.importArtifact(tempDir.toPath(), true)
        );

        assertThat(importException.getType()).isEqualTo(MODEL_NOT_FOUND);
        assertThat(importException.getMessage()).isEqualTo("Could not load component, artifact model file not found");
        final Collection<String> modelfiles = (Collection<String>) importException.getInfos().get("modelfiles");
        assertThat(modelfiles).containsOnly("page.json", "widget.json", "fragment.json");
    }
}
