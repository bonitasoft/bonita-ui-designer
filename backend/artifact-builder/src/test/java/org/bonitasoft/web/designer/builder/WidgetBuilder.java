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
package org.bonitasoft.web.designer.builder;

import com.fasterxml.jackson.annotation.JsonView;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.page.WebResource;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import java.time.Instant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WidgetBuilder {

    List<Property> properties = new ArrayList<>();
    private String id = UUID.randomUUID().toString();
    private String name = "aName";
    private boolean custom = false;
    private String template = "<h1>this is a template</h1>";
    private String controller;
    private AssetBuilder[] assetBuilders;
    private Instant lastUpdate;
    private Set<String> modules = new HashSet<>();
    private Set<String> authRules;
    private String designerVersion;
    private String modelVersion;
    private String previousDesignerVersion;
    private String previousArtifactVersion;
    private String type;
    private boolean favorite = false;
    private boolean hasHelp = false;

    private Set<WebResource> webResources= new HashSet<>();
    private MigrationStatusReport migrationStatusReport = new MigrationStatusReport(true, true);

    public static WidgetBuilder aWidget() {
        return new WidgetBuilder();
    }

    public WidgetBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public WidgetBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public WidgetBuilder custom() {
        this.custom = true;
        return this;
    }

    public WidgetBuilder template(String template) {
        this.template = template;
        return this;
    }

    public WidgetBuilder controller(String controller) {
        this.controller = controller;
        return this;
    }

    public WidgetBuilder property(PropertyBuilder param) {
        return property(param.build());
    }

    public WidgetBuilder property(Property param) {
        properties.add(param);
        return this;
    }

    public WidgetBuilder lastUpdate(Instant instant) {
        this.lastUpdate = instant;
        return this;
    }

    public WidgetBuilder modules(String... modules) {
        this.modules = Set.of(modules);
        return this;
    }

    public WidgetBuilder authRules(String... rules) {
        this.authRules = Set.of(rules);
        return this;
    }

    public WidgetBuilder assets(AssetBuilder... assetBuilders) {
        this.assetBuilders = assetBuilders;
        return this;
    }

    public WidgetBuilder designerVersion(String version) {
        this.designerVersion = version;
        return this;
    }

    public WidgetBuilder modelVersion(String version) {
        this.modelVersion = version;
        return this;
    }

    public WidgetBuilder previousDesignerVersion(String previousDesignerVersion) {
        this.previousDesignerVersion = previousDesignerVersion;
        return this;
    }

    public WidgetBuilder previousArtifactVersion(String previousArtifactVersion) {
        this.previousArtifactVersion = previousArtifactVersion;
        return this;
    }

    public WidgetBuilder favorite() {
        this.favorite = true;
        return this;
    }

    public WidgetBuilder notFavorite() {
        this.favorite = false;
        return this;
    }

    public WidgetBuilder type(String type) {
        this.type = type;
        return this;
    }

    public WidgetBuilder withMigrationStatusReport(MigrationStatusReport migrationStatusReport) {
        this.migrationStatusReport = migrationStatusReport;
        return this;
    }

    public WidgetBuilder isCompatible(boolean compatible) {
        this.migrationStatusReport.setCompatible(compatible);
        return this;
    }

    public WidgetBuilder isMigration(boolean migration) {
        this.migrationStatusReport.setMigration(migration);
        return this;
    }


    public WidgetBuilder withWebResources(WebResource... webResources) {
        this.webResources = Set.of(webResources);
        return this;
    }


    public Widget build() {
        Widget widget = new Widget();
        widget.setId(id);
        widget.setName(name);
        widget.setCustom(custom);
        widget.setTemplate(template);
        widget.setLastUpdate(lastUpdate);
        widget.setModelVersion(modelVersion);
        widget.setDesignerVersion(designerVersion);
        widget.setFavorite(favorite);
        widget.setHasHelp(hasHelp);
        widget.setType(type);
        widget.setWebResources(webResources);
        if (previousArtifactVersion != null) {
            widget.setPreviousArtifactVersion(previousArtifactVersion);
        } else {
            widget.setPreviousDesignerVersion(previousDesignerVersion);
        }
        if (controller != null) {
            widget.setController(controller);
        }
        widget.setStatus(migrationStatusReport);

        if (assetBuilders != null) {
            for (AssetBuilder assetBuilder : assetBuilders) {
                widget.getAssets().add(assetBuilder.withComponentId(id).build());
            }
        }
        for (Property property : properties) {
            widget.addProperty(property);
        }
        widget.setRequiredModules(modules);
        widget.setAuthRules(authRules);
        return widget;
    }
}
