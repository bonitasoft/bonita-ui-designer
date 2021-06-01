package org.bonitasoft.web.designer.config;

import java.nio.file.Path;

public class UiDesignerPropertiesBuilder {

    private String version = "1.13.0-SNAPSHOT";
    private String edition = "Community";
    private String modelVersion = "2.2";
    private boolean experimental = false;

    private UiDesignerProperties.BonitaProperties bonita = new UiDesignerProperties.BonitaProperties();
    private WorkspaceProperties workspace = new WorkspaceProperties();
    private WorkspaceUidProperties workspaceUid = new WorkspaceUidProperties();


    public UiDesignerPropertiesBuilder edition(String edition) {
        this.edition = edition;
        return this;
    }

    public UiDesignerPropertiesBuilder version(String version) {
        this.version = version;
        return this;
    }

    public UiDesignerPropertiesBuilder modelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    public UiDesignerPropertiesBuilder experimental(boolean experimental) {
        this.experimental = experimental;
        return this;
    }

    public UiDesignerPropertiesBuilder portal(String url, String user, String password) {
        this.bonita.getPortal().setUrl(url);
        this.bonita.getPortal().setUser(user);
        this.bonita.getPortal().setPassword(password);
        return this;
    }

    public UiDesignerPropertiesBuilder bdm(String url) {
        this.bonita.getBdm().setUrl(url);
        return this;
    }

    public UiDesignerPropertiesBuilder studioUrl(String url) {
        this.workspace.setApiUrl(url);
        return this;
    }

    public UiDesignerPropertiesBuilder workspaceUidPath(String path) {
        var workdir = Path.of(path);
        this.workspaceUid.setPath(workdir);
        this.workspaceUid.setExtractPath(workdir.resolve("extract"));
        return this;
    }

    public UiDesignerPropertiesBuilder workspacePath(String path) {
        var projectPath = Path.of(path);
        this.workspace.setPath(projectPath);
        this.workspace.getWidgets().setDir(projectPath.resolve("widgets"));
        this.workspace.getWidgetsWc().setDir(projectPath.resolve("widgetsWc"));
        this.workspace.getFragments().setDir(projectPath.resolve("fragments"));
        this.workspace.getPages().setDir(projectPath.resolve("pages"));
        return this;
    }

    public UiDesignerProperties build() {
        var properties = new UiDesignerProperties();
        properties.setVersion(version);
        properties.setEdition(edition);
        properties.setModelVersion(modelVersion);
        properties.setExperimental(experimental);
        properties.setBonita(bonita);
        properties.setWorkspaceUid(workspaceUid);
        properties.setWorkspace(workspace);
        return properties;
    }

}
