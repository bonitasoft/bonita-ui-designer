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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.Files.walkFileTree;
import static java.nio.file.Paths.get;

public class Zipper implements AutoCloseable {

    public static final PathPredicate ALL_DIRECTORIES = path -> true;
    public static final FilePredicate ALL_FILES = file -> true;
    private final ZipOutputStream zip;

    public Zipper(OutputStream destStream) {
        zip = new ZipOutputStream(destStream);
    }

    public void addToZip(byte[] bytes, String destFilename) throws IOException {
        zip.putNextEntry(new ZipEntry(destFilename));
        zip.write(bytes);
        zip.closeEntry();
    }

    public void addToZip(Path path, String destFilename) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        addToZip(bytes, destFilename);
    }

    /**
     * Adds the contents of the given source directory to the zip, by accepting only directories and files accepted by the
     * given predicates.
     *
     * @param sourceDirectory          the source directory
     * @param directoryPredicate       the predicate used to accept directories
     * @param filePredicate            the predicate used to accept files
     * @param destinationDirectoryName the name of the target directory, in the zip, where all the files must be added.
     * @throws IOException
     */
    public void addDirectoryToZip(final Path sourceDirectory,
                                  final PathPredicate directoryPredicate,
                                  final FilePredicate filePredicate,
                                  final String destinationDirectoryName) throws IOException {

        walkFileTree(sourceDirectory, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!directoryPredicate.accept(dir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (filePredicate.accept(file.toFile())) {
                    addToZip(file, normalizeZipEntryName(get(destinationDirectoryName).resolve(sourceDirectory.relativize(file))));
                }
                return FileVisitResult.CONTINUE;
            }

        });
    }

    public void close() throws IOException {
        zip.flush();
        zip.close();
    }

    private String normalizeZipEntryName(Path path) {
        return path.toString().replace(File.separator, "/");
    }

    public interface PathPredicate {
        boolean accept(Path path);
    }

    public interface FilePredicate {
        boolean accept(File file);
    }
}
