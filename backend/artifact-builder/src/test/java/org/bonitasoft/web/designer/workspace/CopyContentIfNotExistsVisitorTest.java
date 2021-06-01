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
package org.bonitasoft.web.designer.workspace;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static java.nio.file.Files.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CopyContentIfNotExistsVisitorTest {

    @Rule
    public TemporaryFolder directory = new TemporaryFolder();

    File source;

    File destination;

    CopyContentIfNotExistsVisitor visitor;

    @Before
    public void setup() throws Exception {
        source = directory.newFolder("source");
        destination = directory.newFolder("destination");
        visitor = new CopyContentIfNotExistsVisitor(source.toPath(), destination.toPath());
    }

    @Test
    public void should_create_given_directory() throws Exception {

        visitor.preVisitDirectory(source.toPath().resolve("widgets"), null);

        assertThat(exists(destination.toPath().resolve("widgets"))).isTrue();
    }

    @Test
    public void should_not_create_given_directory_if_it_already_exist() throws Exception {
        createDirectories(destination.toPath().resolve("widgets"));

        visitor.preVisitDirectory(source.toPath().resolve("widgets"), null);

        assertThat(exists(destination.toPath().resolve("widgets"))).isTrue();
    }

    @Test
    public void should_copy_given_file_from_source_directory() throws Exception {
        write(source.toPath().resolve("pbButton.json"), "contents".getBytes());

        visitor.visitFile(source.toPath().resolve("pbButton.json"), null);

        assertThat(readAllBytes(destination.toPath().resolve("pbButton.json"))).isEqualTo("contents".getBytes());
    }

    @Test
    public void should_not_copy_given_file_from_source_directory_if_it_already_exist() throws Exception {
        write(source.toPath().resolve("pbButton.json"), "contents from source".getBytes());
        write(destination.toPath().resolve("pbButton.json"), "contents from destination".getBytes());

        visitor.visitFile(source.toPath().resolve("pbButton.json"), null);

        assertThat(readAllBytes(destination.toPath().resolve("pbButton.json"))).isEqualTo("contents from destination".getBytes());
    }
}
