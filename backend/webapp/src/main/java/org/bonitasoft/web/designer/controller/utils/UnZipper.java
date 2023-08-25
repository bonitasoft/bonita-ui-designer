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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.ZipUtil;

@Component
public class UnZipper {

    private final Path temporaryZipPath;

    public UnZipper() throws IOException {
        temporaryZipPath = Files.createTempDirectory("uid-tmp");
    }

    public Path unzipInTempDir(InputStream is, String tempDirPrefix) throws IOException {
        Path tempDirectory = Files.createTempDirectory(temporaryZipPath, tempDirPrefix);
        Path zipFile = writeInDir(is, tempDirectory);
        try {
            ZipUtil.unpack(zipFile.toFile(), tempDirectory.toFile());
        } catch (org.zeroturnaround.zip.ZipException e) {
            throw new ZipException(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(zipFile.toFile());
        }
        return tempDirectory;
    }

    private Path writeInDir(InputStream is, Path tempDirectory) throws IOException {
        Path zipFile = tempDirectory.resolve("zipfile" + new Date().getTime());
        Files.write(zipFile, IOUtils.toByteArray(is));
        return zipFile;
    }

}
