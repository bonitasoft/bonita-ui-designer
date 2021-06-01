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
package org.bonitasoft.web.designer.rendering;

import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static org.bonitasoft.web.designer.utils.assertions.CustomAssertions.assertThat;

/**
 * @author Benjamin Parisel
 */
public class WidgetFileHelperTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_write_content_at_path() throws Exception {
        byte[] content = "Mon content".getBytes();

        Path path = WidgetFileHelper.writeFile(content,temporaryFolder.toPath(),"f8a4574");

        assertThat(path.toFile()).exists();
        assertThat(path.getFileName().toString()).isEqualTo("widgets-f8a4574.js");
    }

    @Test
    public void should_delete_on_folder_all_old_widgets_directives_file() throws Exception {
        File assetsFolder = temporaryFolder.newFolder("maPage", "assets");
        File expectToBeDeletedFile = temporaryFolder.newFile("maPage/assets/widgets-fdsf45741sf.min.js");
        File fragment = temporaryFolder.newFile("maPage/assets/123456.js");
        File expectExistFile = temporaryFolder.newFile("maPage/assets/12345654.json");

        WidgetFileHelper.deleteOldConcatenateFiles(assetsFolder.toPath(), "aa");

        assertThat(expectToBeDeletedFile).doesNotExist();
        assertThat(expectExistFile).exists();
        assertThat(fragment).exists();
    }

    @Test
    public void should_delete_on_root_folder_all_old_widgets_directives_file() throws Exception {
        File assetsFolder = temporaryFolder.newFolder("myFragmentId");
        File fragmentJS = temporaryFolder.newFile("myFragmentId/myFragmentId.js");
        File oldConcatDirectiveFile = temporaryFolder.newFile("myFragmentId/widgets-11111.min.js");
        File descriptorFile = temporaryFolder.newFile("myFragmentId/myFragmentId.json");


        WidgetFileHelper.deleteOldConcatenateFiles(assetsFolder.toPath(), "123");

        assertThat(fragmentJS).exists();
        assertThat(descriptorFile).exists();
        assertThat(oldConcatDirectiveFile).doesNotExist();
    }

    @Test(expected = GenerationException.class)
    public void should_throw_generation_exception_if_not_exist_folder_path_when_write_a_file() throws Exception {
        Path unexistingFile = temporaryFolder.toPath().resolve("FileNotFound");

        WidgetFileHelper.writeFile("Mon content".getBytes(), unexistingFile, "notUsedForThisTest");
    }

    @Test(expected = GenerationException.class)
    public void should_throw_generation_exception_if_not_exist_folder_path_dont_exist_when_delete_a_file() throws Exception {
        Path folderNotFound = temporaryFolder.toPath().resolve("folderNotFound");

        WidgetFileHelper.deleteOldConcatenateFiles(folderNotFound, "notUsedForThisTest");
    }

    @Test
    public void should_delete_files_if_old_files_exists() throws Exception {
        File assetsFolder = temporaryFolder.newFolder("maPage", "assets");
        File expectFileDeleted = temporaryFolder.newFile("maPage/assets/widgets-4576.min.js");
        File expectFileAlreadyExist = temporaryFolder.newFile("maPage/assets/widgets-1z2a3456.min.js");

        WidgetFileHelper.deleteOldConcatenateFiles(assetsFolder.toPath(), "1z2a3456");

        assertThat(expectFileDeleted).doesNotExist();
        assertThat(expectFileAlreadyExist).exists();
    }

    @Test
    public void should_delete_files_if_exists() throws Exception {
        File assetsFolder = temporaryFolder.newFolder("maPage", "js");
        File expectFileDeleted = temporaryFolder.newFile("maPage/js/widgets-4576.min.js");

        WidgetFileHelper.deleteConcatenateFile(assetsFolder.toPath());

        assertThat(expectFileDeleted).doesNotExist();
    }


}
