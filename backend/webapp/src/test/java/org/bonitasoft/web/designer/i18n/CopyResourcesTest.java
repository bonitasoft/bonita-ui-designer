package org.bonitasoft.web.designer.i18n;

import org.bonitasoft.web.designer.Main;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class CopyResourcesTest {

    private static String FOLDER_TO_COPY = "tmpCopyResources";
    private static String TARGET_FOLDER = "tmpCopiedResources";

    private Path targetFolder;

    @Rule
    public TemporaryFolder folderManager = new TemporaryFolder();

    private CopyResources copyResources = new CopyResources();

    @Before
    public void setUp() throws IOException {
        targetFolder = folderManager.newFolder(TARGET_FOLDER).toPath();
    }

    @Test
    public void should_copy_not_empty_resources_only() throws IOException {
        //test
        copyResources.copyResources( targetFolder, FOLDER_TO_COPY);
        Path emptyFile = targetFolder.resolve(FOLDER_TO_COPY).resolve("empty.po");
        Path file = targetFolder.resolve(FOLDER_TO_COPY).resolve("simple.po");
        Path folder = targetFolder.resolve(FOLDER_TO_COPY).resolve("pbAutocomplete");
        Path fileIntoFolder = targetFolder.resolve(FOLDER_TO_COPY).resolve("pbAutocomplete/pbAutocomplete.json");

        assertThat(file).exists();
        assertThat(emptyFile).doesNotExist();
        assertThat(folder).exists();
        assertThat(fileIntoFolder).exists();
    }
}
