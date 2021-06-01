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
package org.bonitasoft.web.designer.controller.export;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zeroturnaround.zip.ZipUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.Paths.get;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.controller.export.Zipper.ALL_DIRECTORIES;
import static org.bonitasoft.web.designer.controller.export.Zipper.ALL_FILES;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ZipperTest {

    private Zipper zipper;
    private ByteArrayOutputStream out;
    private Path tmpDir;
    private URI dir;

    @Before
    public void setUp() throws Exception {
        out = new ByteArrayOutputStream();
        zipper = new Zipper(out);
        dir = getClass().getResource("/aDirectory").toURI();
    }

    @After
    public void tearDown() throws IOException {
        zipper.close();
        out.close();
        if (tmpDir != null) {
            FileUtils.deleteQuietly(tmpDir.toFile());
        }
    }

    private Path unzip(ByteArrayOutputStream out) throws IOException {
        InputStream byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
        tmpDir = Files.createTempDirectory("testZip");
        ZipUtil.unpack(byteArrayInputStream, tmpDir.toFile());
        return tmpDir;
    }

    private void expectSameDirContent(final Path actual, final Path expected) throws IOException {
        Files.walkFileTree(actual, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path expectedFile = expected.resolve(actual.relativize(file));
                assertThat(expectedFile.toFile()).exists();
                assertThat(expectedFile.toFile()).hasContent(new String(Files.readAllBytes(expectedFile)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path expectedDir = expected.resolve(actual.relativize(dir));
                assertThat(expectedDir.toFile()).exists();
                return FileVisitResult.CONTINUE;
            }

        });
    }

    @Test
    public void should_zip_a_directory() throws Exception {

        zipper.addDirectoryToZip(get(dir), ALL_DIRECTORIES, ALL_FILES, "");

        Path dest = unzip(out);
        expectSameDirContent(dest, get(dir));
    }

    @Test
    public void should_zip_a_directory_in_a_destination_folder_without_filter() throws Exception {
        zipper.addDirectoryToZip(get(dir), ALL_DIRECTORIES, ALL_FILES, "destinationInZip");

        Path dest = unzip(out);
        expectSameDirContent(dest.resolve("destinationInZip"), get(dir));
    }

    @Test
    public void should_zip_a_directory_in_a_destination_folder() throws Exception {
        zipper.addDirectoryToZip(get(dir), new IncludeChildDirectoryPredicate(get(dir), singleton("aSubDirectory")), ALL_FILES, "destinationInZip");

        Path dest = unzip(out);
        expectSameDirContent(dest.resolve("destinationInZip"), get(dir));
    }

    @Test
    public void should_zipentry_contains_paths_instead_of_file_separators() throws Exception {
        zipper.addDirectoryToZip(get(dir), new IncludeChildDirectoryPredicate(get(dir), singleton("aSubDirectory")), ALL_FILES, "destinationInZip");

        Set<String> entries = zipEntries(out);
        assertThat(entries).contains("destinationInZip/aFile.txt");
    }

    private Set<String> zipEntries(ByteArrayOutputStream out) throws IOException {
        Set<String> entries = new HashSet<>();
        try (ZipInputStream zipFile = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
            ZipEntry entry;
            while ((entry = zipFile.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }
        return entries;
    }

    @Test
    public void should_zip_a_directory_and_filter_out_unaccepted_subdirectories() throws Exception {
        String destinationInZip = "destinationInZip";
        zipper.addDirectoryToZip(get(dir), new IncludeChildDirectoryPredicate(get(dir), singleton("nonExisting")), ALL_FILES, destinationInZip);

        Path dest = unzip(out);
        assertThat(dest.resolve(destinationInZip).toFile().list()).containsExactly("aFile.txt");
    }

    @Test
    public void should_zip_a_directory_and_filter_out_unaccepted_descriptorjsonfile() throws Exception {
        String destinationInZip = "destinationInZip";
        zipper.addDirectoryToZip(get(this.getClass().getResource("/workspace/pages/ma-page").toURI()), ALL_DIRECTORIES, new ExcludeDescriptorFilePredicate("ma-page.json"), destinationInZip);

        Path dest = unzip(out);
        assertThat(dest.resolve(destinationInZip).toFile().list()).doesNotContain("ma-page.json");
    }

    @Test
    public void should_zip_some_bytes() throws Exception {
        byte[] foo = "foobar".getBytes(StandardCharsets.UTF_8);

        zipper.addToZip(foo, "foo.txt");

        Path dest = unzip(out);
        assertThat(dest.resolve("foo.txt").toFile()).exists();
        assertThat(dest.resolve("foo.txt").toFile()).usingCharset(StandardCharsets.UTF_8).hasContent("foobar");
    }

    @Test
    public void should_close_stream() throws Exception {
        ByteArrayOutputStream out = mock(ByteArrayOutputStream.class);
        Zipper zipper = new Zipper(out);

        zipper.close();

        verify(out).flush();
        verify(out).close();
    }
}
