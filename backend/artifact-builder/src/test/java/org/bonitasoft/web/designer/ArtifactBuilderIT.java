package org.bonitasoft.web.designer;

import org.bonitasoft.web.designer.config.UiDesignerPropertiesBuilder;
import org.bonitasoft.web.designer.model.ModelException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.assertj.core.api.Assertions.assertThat;

public class ArtifactBuilderIT {


    @Test
    public void standalone_run() throws ModelException, IOException {

        // Given
        var properties = new UiDesignerPropertiesBuilder()
                .workspacePath("./target/ArtifactBuilderIT/project")
                .workspaceUidPath("./target/ArtifactBuilderIT/uid")
                .build();

        // prepare dummy page in workspace
        var pageId = "ma-page";
        var target = properties.getWorkspace().getPages().getDir().resolve(pageId);
        deleteDirectory(target.toFile());
        var source = Path.of("./src/test/resources/workspace/pages/" + pageId);
        copyDirectory(source.toFile(), target.toFile());

        // When

        // ====================

        var factory = new ArtifactBuilderFactory(properties);
        var artifactBuilder = factory.create();

        var page = artifactBuilder.buildPage(pageId);

//        Files.write(Path.of("./target/ArtifactBuilderIT/page.zip"), page);

        // ====================

//        var jsonHandler = new JsonHandlerFactory().create();
//        var core = new UiDesignerCoreFactory(properties, jsonHandler).create();
//        var builder = new ArtifactBuilderFactory(properties,jsonHandler,core).create();
//
//        var myPage = core.getPageService().get(pageId);
//        var zip = builder.build(myPage);

        // ====================

        // Then
        assertThat(page).isNotEmpty();
    }
}
