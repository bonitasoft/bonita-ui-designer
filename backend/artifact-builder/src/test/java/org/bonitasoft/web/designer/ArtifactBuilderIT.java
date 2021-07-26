package org.bonitasoft.web.designer;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.UiDesignerPropertiesBuilder;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.controller.utils.Unzipper;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.FileUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ArtifactBuilderIT {

    private UiDesignerProperties properties;
    private ArtifactBuilder artifactBuilder;

    private Unzipper unziper;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    public ArtifactBuilderIT() throws IOException {
        unziper = new Unzipper();
    }

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
    public void should_import_cutom_widget_without_prefix() throws Exception {

        // Given
        Path timelineWidgetToImportPath = tempFolder.newFolderPath("timelineWidget");
        FileUtils.copyDirectory(Path.of("src/test/resources/import/timelineWidget").toFile(), timelineWidgetToImportPath.toFile());

        // When
        artifactBuilder = new ArtifactBuilderFactory(properties).create();

        ImportReport importReport = artifactBuilder.importWidget(timelineWidgetToImportPath, true);

        String loadedControler = ((Widget)importReport.getElement()).getController();
        String loadedTemplate = ((Widget)importReport.getElement()).getTemplate();

        var widget = artifactBuilder.buildWidget("timelineWidget");

        Path timelineWidgetPath = unziper.unzipInTempDir(new ByteArrayInputStream(widget), "timelineWidget");

        // Then
        assertThat(loadedControler).doesNotStartWith("@");
        assertThat(loadedTemplate).doesNotStartWith("@");

        assertThat(timelineWidgetPath.resolve("resources/widget.json")).exists();
        assertThat(Files.readString(timelineWidgetPath.resolve("resources/widget.json"))).contains("\"template\":\"@timelineWidget.tpl.html\",");
        assertThat(Files.readString(timelineWidgetPath.resolve("resources/widget.json"))).contains("\"controller\":\"@timelineWidget.ctrl.js\",");

        assertThat(timelineWidgetPath.resolve("resources/timelineWidget.ctrl.js")).exists();
        assertThat(timelineWidgetPath.resolve("resources/timelineWidget.ctrl.js").toFile()).hasContent(loadedControler);

        assertThat(timelineWidgetPath.resolve("resources/timelineWidget.tpl.html")).exists();
        assertThat(timelineWidgetPath.resolve("resources/timelineWidget.tpl.html").toFile()).hasContent(loadedTemplate);

        assertThat(timelineWidgetPath.resolve("resources/timelineWidget.js")).exists();
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
        await().atMost(2, SECONDS).untilAsserted(() ->
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
