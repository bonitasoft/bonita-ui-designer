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
package org.bonitasoft.web.designer.controller.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.MODEL_NOT_FOUND;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.UNEXPECTED_ZIP_STRUCTURE;
import static org.bonitasoft.web.designer.controller.importer.exception.ImportExceptionMatcher.hasType;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.Loader;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ImporterResolverTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ImporterResolver importerResolver;

    private ArtifactImporter<Page> pageArtifactImporter;
    private ArtifactImporter<Widget> widgetArtifactImporter;

    @Before
    public void setUp() throws Exception {
        pageArtifactImporter = new ArtifactImporter<>(mock(Repository.class), mock(Loader.class));
        widgetArtifactImporter = new ArtifactImporter<>(mock(Repository.class), mock(Loader.class));
        importerResolver = new ImporterResolver(new DesignerConfig().artifactImporters(pageArtifactImporter, widgetArtifactImporter));
    }

    @Test
    public void should_get_page_importer_from_artifact_type() throws Exception {
        ArtifactImporter importer = importerResolver.getImporter("page");

        assertThat(importer).isEqualTo(pageArtifactImporter);
    }

    @Test
    public void should_get_widget_importer_from_artifact_type() throws Exception {
        ArtifactImporter importer = importerResolver.getImporter("widget");

        assertThat(importer).isEqualTo(widgetArtifactImporter);
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_NotFoundException_for_an_unknown_artifact_type() throws Exception {
        importerResolver.getImporter("unknown");
    }

    @Test
    public void should_get_page_importer_from_path() throws Exception {
        Path resources = tempDir.newFolderPath("resources");
        Files.createFile(resources.resolve("page.json"));

        ArtifactImporter importer = importerResolver.getImporter(tempDir.toPath());

        assertThat(importer).isEqualTo(pageArtifactImporter);
    }

    @Test
    public void should_get_widget_importer_from_path() throws Exception {
        Path resources = tempDir.newFolderPath("resources");
        Files.createFile(resources.resolve("widget.json"));

        ArtifactImporter importer = importerResolver.getImporter(tempDir.toPath());

        assertThat(importer).isEqualTo(widgetArtifactImporter);
    }

    @Test
    public void should_get_ImportException_while_resources_folder_is_absent() throws Exception {
        exception.expect(ImportException.class);
        exception.expect(hasType(UNEXPECTED_ZIP_STRUCTURE));
        exception.expectMessage("Incorrect zip structure resources folder is needed");

        importerResolver.getImporter(tempDir.toPath());
    }

    @Test
    public void should_get_ImportException_while_no_importer_found_from_path() throws Exception {
        tempDir.newFolderPath("resources");
        try {
            importerResolver.getImporter(tempDir.toPath());
            failBecauseExceptionWasNotThrown(ImportException.class);
        } catch (ImportException e) {
            assertThat(e.getMessage()).isEqualTo("Could not load component, artifact model file not found");
            assertThat(e.getType()).isEqualTo(MODEL_NOT_FOUND);
            assertThat((Collection<String>) e.getInfos().get("modelfiles")).containsOnly("page.json", "widget.json");
        }
    }
}
