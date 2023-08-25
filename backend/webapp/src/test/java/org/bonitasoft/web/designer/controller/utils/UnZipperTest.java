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
package org.bonitasoft.web.designer.controller.utils;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UnZipperTest {

    private UnZipper unzipper;

    @BeforeEach
    public void setUp() throws IOException {
        unzipper = new UnZipper();
    }

    @Test
    public void should_throw_zip_exception_when_inpustream_cannot_be_unzipped() throws Exception {
        InputStream stream = UnZipperTest.class.getResourceAsStream("notAzipFile.txt");

        assertThrows(ZipException.class, () ->  unzipper.unzipInTempDir(stream, "aPrefix"));

        // clean
        //FileUtils.deleteDirectory(unzipInTempDir.toFile());
    }

    @Test
    public void should_unzip_a_zip_file_into_a_temp_folder() throws Exception {
        InputStream stream = UnZipperTest.class.getResourceAsStream("azipFile.zip");

        Path unzipInTempDir = unzipper.unzipInTempDir(stream, "aPrefix");

        assertThat(unzipInTempDir.resolve("notAzipFile.txt").toFile()).exists();

        // clean
        FileUtils.deleteDirectory(unzipInTempDir.toFile());
    }
}
