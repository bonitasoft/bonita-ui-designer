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

import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ImportStoreTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private AbstractArtifactImporter artifactImporter;

    private ImportStore importStore;

    @Before
    public void setUp() throws Exception {
        importStore = new ImportStore();
    }

    @Test
    public void should_store_import() throws Exception {
        Path importPath = Paths.get("import/path");

        Import storedImport = importStore.store(artifactImporter, importPath);

        assertThat(storedImport.getUUID()).isNotNull();
        assertThat(storedImport.getImporter()).isEqualTo(artifactImporter);
        assertThat(storedImport.getPath()).isEqualTo(importPath);
    }

    @Test
    public void should_get_a_stored_import() throws Exception {
        Import expectedImport = importStore.store(artifactImporter, Paths.get("import/path"));

        Import fetchedImport = importStore.get(expectedImport.getUUID());

        assertThat(expectedImport).isEqualTo(fetchedImport);
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_not_found_exception_while_getting_an_unknown_import() throws Exception {
        importStore.get("unknown-import");
    }

    @Test(expected = NotFoundException.class)
    public void should_remove_a_stored_import() throws Exception {
        Import addedReport = importStore.store(artifactImporter, Paths.get("import/path"));

        importStore.remove(addedReport.getUUID());

        importStore.get(addedReport.getUUID()); // should throw not found exception
    }

    @Test
    public void should_delete_folder_while_removing_a_stored_import() throws Exception {
        Path importFolder = temporaryFolder.newFolderPath("importFolder");
        Import addedReport = importStore.store(artifactImporter, importFolder);

        importStore.remove(addedReport.getUUID());

        assertThat(importFolder).doesNotExist();
    }

    @Test
    public void should_fail_silently_while_removing_an_unexisting_import() throws Exception {

        importStore.remove("unexinting id");

        // ok expected no exception
    }
}
