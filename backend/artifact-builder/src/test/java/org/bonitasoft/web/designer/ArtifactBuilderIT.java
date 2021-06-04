package org.bonitasoft.web.designer;

import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.UiDesignerPropertiesBuilder;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

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
                .workspacePath("./target/ArtifactBuilderIT/project")
                .workspaceUidPath("./target/ArtifactBuilderIT/uid")
                .build();

        await().atMost(1, SECONDS).until(() -> {
            try {
                deleteDirectory(properties.getWorkspace().getPages().getDir().toFile());
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
        var target = properties.getWorkspace().getPages().getDir().resolve(pageId);
        var source = Path.of("./src/test/resources/workspace/pages/" + pageId);
        copyDirectory(source.toFile(), target.toFile());

        // When
        artifactBuilder = new ArtifactBuilderFactory(properties).create();
        var page = artifactBuilder.buildPage(pageId);

        // Then
        assertThat(page).isNotEmpty();
    }

    @Test
    public void should_index_pages() throws Exception {
        // Given
        var pageId = "ma-page";
        var target = properties.getWorkspace().getPages().getDir().resolve(pageId);
        var source = Path.of("./src/test/resources/workspace/pages/" + pageId);
        copyDirectory(source.toFile(), target.toFile());
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
        copyDirectory(source.toFile(), target.toFile());
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
}
