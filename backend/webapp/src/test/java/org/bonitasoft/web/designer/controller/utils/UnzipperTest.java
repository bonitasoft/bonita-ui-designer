/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.controller.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class UnzipperTest {

    private Unzipper unzipper;

    @Before
    public void setUp() {
        unzipper = new Unzipper();
    }

    @Test(expected = ZipException.class)
    public void should_throw_zip_exception_when_inpustream_cannot_be_unzipped() throws Exception {
        InputStream stream = UnzipperTest.class.getResourceAsStream("notAzipFile.txt");

        Path unzipInTempDir = unzipper.unzipInTempDir(stream, "aPrefix");

        // clean
        FileUtils.deleteDirectory(unzipInTempDir.toFile());
    }

    @Test
    public void should_unzip_a_zip_file_into_a_temp_folder() throws Exception {
        InputStream stream = UnzipperTest.class.getResourceAsStream("azipFile.zip");

        Path unzipInTempDir = unzipper.unzipInTempDir(stream, "aPrefix");

        assertThat(unzipInTempDir.resolve("notAzipFile.txt").toFile()).exists();

        // clean
        FileUtils.deleteDirectory(unzipInTempDir.toFile());
    }
}
