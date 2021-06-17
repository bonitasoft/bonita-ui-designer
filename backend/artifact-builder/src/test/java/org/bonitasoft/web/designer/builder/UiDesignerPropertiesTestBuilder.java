package org.bonitasoft.web.designer.builder;

import org.bonitasoft.web.designer.config.DesignerInitializerException;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceProperties;

import java.nio.file.Path;

public class UiDesignerPropertiesTestBuilder {

    private Path workspacePath;

    public static UiDesignerPropertiesTestBuilder aUiDesignerPropertiesBuilder() {
        return new UiDesignerPropertiesTestBuilder();
    }

    public static UiDesignerProperties aUiDesignerProperties(Path workspacePath) {
        return aUiDesignerPropertiesBuilder().withWorkspacePath(workspacePath).build();
    }

    private UiDesignerPropertiesTestBuilder withWorkspacePath(Path workspacePath) {
        this.workspacePath = workspacePath;
        return this;
    }

    public UiDesignerProperties build() {

        if(workspacePath == null){
            throw new DesignerInitializerException("Workspace Path can not be null.");
        }

        UiDesignerProperties uiDesignerProperties = new UiDesignerProperties();

        WorkspaceProperties workspaceProperties = uiDesignerProperties.getWorkspace();
        workspaceProperties.getPages().setDir(workspacePath.resolve("pages"));
        workspaceProperties.getWidgets().setDir(workspacePath.resolve("widgets"));
        workspaceProperties.getFragments().setDir(workspacePath.resolve("fragments"));

        return uiDesignerProperties;
    }

}
