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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Sets;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.joda.time.Instant;

public class WidgetBuilder {

    private String id = "anId";
    private String name = "aName";
    private boolean custom = false;
    private String template = "<h1>this is a template</h1>";
    List<Property> properties = new ArrayList<>();
    private AssetBuilder[] assetBuilders;
    private Instant lastUpdate;
    private HashSet<String> modules;
    private String version;
    private boolean favorite = false;

    public static WidgetBuilder aWidget() {
        return new WidgetBuilder();
    }

    public WidgetBuilder id(String id) {
        this.id = id;
        return this;
    }

    public WidgetBuilder name(String name) {
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
        this.modules = Sets.<String>newHashSet(modules);
        return this;
    }

    public WidgetBuilder assets(AssetBuilder... assetBuilders) {
        this.assetBuilders = assetBuilders;
        return this;
    }

    public WidgetBuilder version(String version) {
        this.version = version;
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

    public Widget build() {
        Widget widget = new Widget();
        widget.setId(id);
        widget.setName(name);
        widget.setCustom(custom);
        widget.setTemplate(template);
        widget.setLastUpdate(lastUpdate);
        widget.setDesignerVersion(version);
        widget.setFavorite(favorite);

        if (assetBuilders != null) {
            for (AssetBuilder assetBuilder : assetBuilders) {
                widget.getAssets().add(assetBuilder.withComponentId(id).build());
            }
        }
        for (Property property : properties) {
            widget.addProperty(property);
        }
        widget.setRequiredModules(modules);
        return widget;
    }
}
