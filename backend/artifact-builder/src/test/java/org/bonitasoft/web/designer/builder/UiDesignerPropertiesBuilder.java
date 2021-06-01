package org.bonitasoft.web.designer.builder;

import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceProperties;

import java.nio.file.Path;

public class UiDesignerPropertiesBuilder {

    private String modelVersion = "2.0";

    private Path workspacePath;

    public static UiDesignerPropertiesBuilder aUiDesignerPropertiesBuilder() {
        return new UiDesignerPropertiesBuilder();
    }

    public static UiDesignerProperties aUiDesignerProperties(Path workspacePath) {
        return aUiDesignerPropertiesBuilder().withWorkspacePath(workspacePath).build();
    }

    private UiDesignerPropertiesBuilder withWorkspacePath(Path workspacePath) {
        this.workspacePath = workspacePath;
        return this;
    }

    public UiDesignerProperties build() {
        UiDesignerProperties uiDesignerProperties = new UiDesignerProperties();

        uiDesignerProperties.setModelVersion(modelVersion);

        WorkspaceProperties workspaceProperties = new WorkspaceProperties();
        workspaceProperties.getPages().setDir(workspacePath.resolve("pages"));
        workspaceProperties.getWidgets().setDir(workspacePath.resolve("widgets"));
        workspaceProperties.getWidgetsWc().setDir(workspacePath.resolve("widgetsWc"));
        workspaceProperties.getFragments().setDir(workspacePath.resolve("fragments"));
        uiDesignerProperties.setWorkspace(workspaceProperties);

        return uiDesignerProperties;
    }

}
