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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class FilesConcatenator {

    public static byte[] concat(List<Path> filePathToConcatenate) {
        try (var bOutput = new ByteArrayOutputStream()) {
            filePathToConcatenate.forEach(path -> writeConcatInOutput(bOutput, path));
            return bOutput.toByteArray();
        } catch (IOException e) {
            throw new GenerationException("Error while content generating ", e);
        }
    }

    private static void writeConcatInOutput(ByteArrayOutputStream output, Path path) {
        try {
            output.write(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new GenerationException("Error while content generating ", e);
        }
    }
}
