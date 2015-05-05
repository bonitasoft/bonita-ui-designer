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
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper implements AutoCloseable {

    private static final PathPredicate ACCEPT_ALL = new PathPredicate() {

        @Override
        public boolean accept(Path path) {
            return true;
        }
    };

    private ZipOutputStream zip;

    public Zipper(OutputStream destStream) {
        zip = new ZipOutputStream(destStream);
    }

    public void addDirectoryToZip(final Path directory) throws IOException {
        addDirectoryToZip(directory, "");
    }

    public void addDirectoryToZip(final Path directory, final String destinationDirectoryName) throws IOException {
        addDirectoryToZip(directory, ACCEPT_ALL, destinationDirectoryName);
    }

    /**
     * Adds the contents of the given source directory to the zip, by accepting only the direct subdirectories whose
     * name is contained in the given accepted names.
     *
     * @param sourceDirectory the source directory
     * @param acceptedChildDirectoryNames the names of the accepted direct subdirectory names
     * @param destinationDirectoryName the name of the target directory, in the zip, where all the files must be added.
     * @throws IOException
     */
    public void addDirectoryToZip(final Path sourceDirectory,
            final Set<String> acceptedChildDirectoryNames,
            final String destinationDirectoryName) throws IOException {
        PathPredicate predicate = new PathPredicate() {

            @Override
            public boolean accept(Path path) {
                return !path.getParent().equals(sourceDirectory)
                        || acceptedChildDirectoryNames.contains(path.getFileName().toString());
            }
        };
        addDirectoryToZip(sourceDirectory, predicate, destinationDirectoryName);
    }

    /**
     * Adds the contents of the given source directory to the zip, by accepting only the directories accepted by the
     * given predicate.
     *
     * @param sourceDirectory the source directory
     * @param directoryPredicate the predicate used to accept directories
     * @param destinationDirectoryName the name of the target directory, in the zip, where all the files must be added.
     * @throws IOException
     */
    private void addDirectoryToZip(final Path sourceDirectory,
            final PathPredicate directoryPredicate,
            final String destinationDirectoryName) throws IOException {
        Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!directoryPredicate.accept(dir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                addToZip(file, normalizeZipEntryName(Paths.get(destinationDirectoryName).resolve(sourceDirectory.relativize(file))));
                return FileVisitResult.CONTINUE;
            }

        });
    }

    private String normalizeZipEntryName(Path path) {
        String pathAsString = path.toString();
        return pathAsString.replace(File.separator, "/");
    }

    public void addToZip(Path path, String destFilename) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        addToZip(bytes, destFilename);
    }

    public void addToZip(byte[] bytes, String destFilename) throws IOException {
        zip.putNextEntry(new ZipEntry(destFilename));
        zip.write(bytes);
        zip.closeEntry();
    }

    public void close() throws IOException {
        zip.flush();
        zip.close();
    }

    private static interface PathPredicate {

        boolean accept(Path path);
    }
}
