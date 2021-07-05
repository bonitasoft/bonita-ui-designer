package org.bonitasoft.web.designer;

import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.UiDesignerPropertiesBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.FileUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ArtifactBuilderIT {

    private UiDesignerProperties properties;
    private ArtifactBuilder artifactBuilder;

    @Before
    public void setUp() throws Exception {
        properties = new UiDesignerPropertiesBuilder()
                .workspacePath(Path.of("./target/ArtifactBuilderIT/project"))
                .workspaceUidPath(Path.of("./target/ArtifactBuilderIT/uid"))
                .build();

        await().atMost(1, SECONDS).until(() -> {
            try {
                deleteDirectory(properties.getWorkspace().getPages().getDir().toFile());
                deleteDirectory(properties.getWorkspace().getFragments().getDir().toFile());
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    @Test
    public void should_export_page() throws Exception {

        // Given
        var pageId = "ma-page";
        createPage(pageId);
        createFragment("person");
        createFragment("details");

        // When
        artifactBuilder = new ArtifactBuilderFactory(properties).create();
        var page = artifactBuilder.buildPage(pageId);
        List<String> zipEntriesName = extractZipEntriesName(page);

        // Then
        assertThat(page).isNotEmpty();
        assertThat(zipEntriesName).contains("resources/page.json");
        assertThat(zipEntriesName).contains("resources/fragments/person/person.json");
        assertThat(zipEntriesName).contains("resources/fragments/details/details.json");
    }

    @Test
    public void should_export_fragment() throws Exception {

        // Given
        var fragmentPerson = "person";
        createFragment(fragmentPerson);
        createFragment("details");

        // When
        artifactBuilder = new ArtifactBuilderFactory(properties).create();
        var page = artifactBuilder.buildFragment(fragmentPerson);
        List<String> zipEntriesName = extractZipEntriesName(page);

        // Then
        assertThat(page).isNotEmpty();
        assertThat(zipEntriesName).contains("resources/fragment.json");
        assertThat(zipEntriesName).contains("resources/fragments/details/details.json");
    }

    @Test
    public void should_index_pages() throws Exception {
        // Given
        createPage("ma-page");

        // When
        artifactBuilder = new ArtifactBuilderFactory(properties).create();

        // Then
        await().atMost(1, SECONDS).untilAsserted(() ->
                assertThat(properties.getWorkspace().getPages().getDir().resolve(".metadata/.index.json")).exists()
        );
    }

    @Test
    public void should_watch_pages() throws Exception {
        // Given
        var pageId = "ma-page";
        var target = properties.getWorkspace().getPages().getDir().resolve(pageId);
        var source = Path.of("./src/test/resources/workspace/pages/" + pageId);
        createPage(pageId);

        artifactBuilder = new ArtifactBuilderFactory(properties).create();
        var jsonIndex = properties.getWorkspace().getPages().getDir().resolve(".metadata/.index.json");

        // Delete page folder and index metadata (index should be recreated on the fly when copying back the page folder thanks to watcher)
        deleteDirectory(target.toFile());
        deleteQuietly(jsonIndex.toFile());

        // When
        copyDirectory(source.toFile(), target.toFile());

        // Then
        await().atMost(1, SECONDS).untilAsserted(() ->
                assertThat(jsonIndex).exists()
        );
    }

    private void createPage(String pageId) throws IOException {
        var target = properties.getWorkspace().getPages().getDir().resolve(pageId);
        var source = Path.of("./src/test/resources/workspace/pages/" + pageId);
        copyDirectory(source.toFile(), target.toFile());
    }

    private void createFragment(String fragmentId) throws IOException {
        var target = properties.getWorkspace().getFragments().getDir().resolve(fragmentId);
        var source = Path.of("./src/test/resources/workspace/fragments/" + fragmentId);
        copyDirectory(source.toFile(), target.toFile());
    }

    private List<String> extractZipEntriesName(byte[] content) throws IOException {
        List<String> entries = new ArrayList<>();

        ZipInputStream zi = null;
        try {
            zi = new ZipInputStream(new ByteArrayInputStream(content));

            ZipEntry zipEntry = null;
            while ((zipEntry = zi.getNextEntry()) != null) {
                entries.add(zipEntry.getName());
            }
        } finally {
            if (zi != null) {
                zi.close();
            }
        }
        return entries;
    }
}
