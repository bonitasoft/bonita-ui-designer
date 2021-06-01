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
package org.bonitasoft.web.designer.model;

import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.Files.readAllBytes;
import static org.assertj.core.api.Assertions.fail;

/**
 * For each json provided widgets, we test that it is deserializable in Widget model
 *
 * @author Colin Puy
 */
public class ProvidedWidgetsModelTest {

    @Test
    public void provided_widgets_should_be_deserializable() throws Exception {
        URI widgets = getClass().getResource("/widgets").toURI();
        Files.walkFileTree(Paths.get(widgets), new IsWidgetDeserializableVisitor());
    }

    private final class IsWidgetDeserializableVisitor extends SimpleFileVisitor<Path> {

        private final JsonHandler jsonHandler;

        private IsWidgetDeserializableVisitor() {
            this.jsonHandler = new JsonHandlerFactory().create();
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.endsWith(".json")) {
                try {
                    jsonHandler.fromJson(readAllBytes(file), Widget.class);
                } catch (Exception e) {
                    fail(file.getFileName() + " cannot be deserialized to Widget model", e);
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
