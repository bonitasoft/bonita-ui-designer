package org.bonitasoft.web.designer.builder;

import org.bonitasoft.web.designer.Version;
import org.bonitasoft.web.designer.config.UiDesignerPropertiesBuilder;
import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class UiDesignerPropertiesBuilderTest {

    @Test
    public void default_props_should_be_set() {
        var properties = new UiDesignerPropertiesBuilder()
                .workspacePath(Path.of("some/place/"))
                .build();

        assertThat(properties.getVersion()).isEqualTo(Version.VERSION);
        assertThat(properties.getEdition()).isEqualTo(Version.EDITION);
        assertThat(properties.getModelVersion()).isEqualTo(Version.MODEL_VERSION);
    }

    @Test
    public void default_paths_should_be_set() {
        var workspacePath = Path.of("some", "place");

        var properties = new UiDesignerPropertiesBuilder()
                .workspacePath(workspacePath)
                .build();

        assertThat(properties.getWorkspace().getPath()).isEqualTo(workspacePath);
        assertThat(properties.getWorkspaceUid()).isNotNull();
    }

    @Test
    public void should_override_default_folder_name() {
        var workspacePath = Path.of("some", "place");

        var pageFolder = "web-pages";
        var fragmentFolder = "web-fragments";
        var widgetFolder = "web-widgets";
        var widgetWcFolder = "web-widgetWcs";

        var properties = new UiDesignerPropertiesBuilder()
                .workspacePath(workspacePath)
                .pagesFolderName(pageFolder)
                .fragmentsFolderName(fragmentFolder)
                .widgetsFolderName(widgetFolder)
                .build();

        assertThat(properties.getWorkspace().getPath()).isEqualTo(workspacePath);
        assertThat(properties.getWorkspace().getPages().getDir()).isEqualTo(properties.getWorkspace().getPath().resolve(pageFolder));
    }
}
