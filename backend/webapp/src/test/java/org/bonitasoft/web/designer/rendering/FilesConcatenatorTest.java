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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.google.common.io.Files;
import org.bonitasoft.web.designer.rendering.FilesConcatenator;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Benjamin Parisel
 */
@RunWith(MockitoJUnitRunner.class)
public class FilesConcatenatorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_concatenate_files() throws IOException {
        Files.write("file1".getBytes(), temporaryFolder.newFile("file1.js"));
        Files.write("file2".getBytes(), temporaryFolder.newFile("file2.js"));
        List<Path> files = Arrays.asList(temporaryFolder.toPath().resolve("file1.js"), temporaryFolder.toPath()
                .resolve("file2.js"));

        byte[] content = FilesConcatenator.concat(files);

        assertThat(content).isEqualTo("file1file2".getBytes());
    }

}
