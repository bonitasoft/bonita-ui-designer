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

import org.bonitasoft.web.designer.controller.export.FragmentExporter;
import org.bonitasoft.web.designer.controller.export.PageExporter;
import org.bonitasoft.web.designer.controller.export.WidgetExporter;
import org.bonitasoft.web.designer.controller.importer.AbstractArtifactImporter;
import org.bonitasoft.web.designer.controller.importer.FragmentImporter;
import org.bonitasoft.web.designer.controller.importer.Import;
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
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ImporterResolverTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private ImportStore importStore;

    @Mock
    private PageImporter pageImporter;

    @Mock
    private WidgetImporter widgetImporter;

    @Mock
    private FragmentImporter fragmentImporter;

    private AngularJsArtifactBuilder artifactBuilder;

    @Before
    public void setUp() throws Exception {

        artifactBuilder = spy(new AngularJsArtifactBuilder(
                mock(Workspace.class),
                mock(WidgetService.class),
                mock(FragmentService.class),
                mock(PageService.class),
                mock(PageExporter.class),
                mock(FragmentExporter.class),
                mock(WidgetExporter.class),
                mock(HtmlGenerator.class),
                importStore,
                pageImporter,
                fragmentImporter,
                widgetImporter
        ));

        lenient().when(importStore.store(any(), any())).thenAnswer(invocation -> {
            AbstractArtifactImporter<?> importer = invocation.getArgument(0);
            Path path = invocation.getArgument(1);
            String uuid = UUID.randomUUID().toString();
            return new Import(importer, uuid, path);
        });

    }

    @Test
    public void should_get_page_importer_from_artifact_type() throws Exception {

        // When
        artifactBuilder.importPage(tempDir.toPath(), false);

        // Then
        verify(artifactBuilder).importFromPath(any(), eq(false), eq(pageImporter));
    }

    @Test
    public void should_get_widget_importer_from_artifact_type() throws Exception {
        // When
        artifactBuilder.importWidget(tempDir.toPath(), false);
        // Then
        verify(artifactBuilder).importFromPath(any(), eq(false), eq(widgetImporter));
    }

    @Test
    public void should_throw_NotFoundException_for_an_unknown_artifact_type() throws Exception {
        // Given
        when(widgetImporter.tryToImportAndGenerateReport(any(), eq(false))).thenThrow(new ImportException(PAGE_NOT_FOUND, "error"));
        tempDir.newFolderPath("resources");
        // When
        final Throwable throwable = catchThrowable(() ->
                artifactBuilder.importWidget(tempDir.toPath(), false)
        );
        // Then
        assertThat(throwable).isInstanceOf(ImportException.class);
        ImportException exception = (ImportException) throwable;
        assertThat(exception.getType()).isEqualTo(PAGE_NOT_FOUND);
    }

    @Test
    public void should_get_page_importer_from_path() throws Exception {
        Path resources = tempDir.newFolderPath("resources");
        Files.createFile(resources.resolve("page.json"));
        Path path = tempDir.toPath();

        artifactBuilder.importArtifact(path, false);

        verify(artifactBuilder).importFromPath(eq(path), eq(false), eq(pageImporter));
    }

    @Test
    public void should_get_widget_importer_from_path() throws Exception {
        Path resources = tempDir.newFolderPath("resources");
        Files.createFile(resources.resolve("widget.json"));
        Path path = tempDir.toPath();

        artifactBuilder.importArtifact(path, false);

        verify(artifactBuilder).importFromPath(eq(path), eq(false), eq(widgetImporter));
    }

    @Test
    public void should_get_fragment_importer_from_artifact_type() throws Exception {
        artifactBuilder.importFragment(any(), eq(false));

        verify(artifactBuilder).importFromPath(any(), eq(false), eq(fragmentImporter));
    }

    @Test
    public void should_get_fragment_importer_from_path() throws Exception {
        Path resources = tempDir.newFolderPath("resources");
        Files.createFile(resources.resolve("fragment.json"));
        Path path = tempDir.toPath();

        artifactBuilder.importArtifact(path, false);

        verify(artifactBuilder).importFromPath(eq(path), eq(false), eq(fragmentImporter));
    }

    @Test
    public void should_get_ImportException_while_resources_folder_is_absent() throws Exception {

        // When
        final Throwable throwable = catchThrowable(() -> artifactBuilder.importArtifact(tempDir.toPath(), false));

        // Then
        assertThat(throwable).isInstanceOf(ImportException.class);
        ImportException exception = (ImportException) throwable;
        assertThat(exception.getType()).isEqualTo(UNEXPECTED_ZIP_STRUCTURE);
        assertThat(exception).hasMessage("Incorrect zip structure, resources folder is needed");
    }

    @Test
    public void should_get_ImportException_while_no_importer_found_from_path() throws Exception {
        // Given
        tempDir.newFolderPath("resources");

        // When
        final Throwable throwable = catchThrowable(() -> artifactBuilder.resolveArtifactType(tempDir.toPath()));

        // Then
        ImportException e = (ImportException) throwable;
        assertThat(e.getMessage()).isEqualTo("Could not load component, artifact model file not found");
        assertThat(e.getType()).isEqualTo(MODEL_NOT_FOUND);
        assertThat((Collection<String>) e.getInfos().get("modelfiles")).containsOnly("page.json", "widget.json", "fragment.json");

    }
}
