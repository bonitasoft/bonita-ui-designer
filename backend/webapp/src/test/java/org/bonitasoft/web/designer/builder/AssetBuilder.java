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

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;

public class AssetBuilder {

    private String id = "UUID";
    private String name = "myasset.js";
    private AssetType type = AssetType.JAVASCRIPT;
    private AssetScope scope = AssetScope.PAGE;
    private String componentId;
    private int order = 1;

    public static AssetBuilder anAsset() {
        return new AssetBuilder();
    }

    public static Asset aFilledAsset(Page page) {
        return anAsset().withScope(AssetScope.PAGE).withComponentId(page.getId()).build();
    }

    public AssetBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public AssetBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AssetBuilder withOrder(int order) {
        this.order = order;
        return this;
    }

    public AssetBuilder withType(AssetType type) {
        this.type = type;
        return this;
    }

    public AssetBuilder withScope(AssetScope scope) {
        this.scope = scope;
        return this;
    }

    public AssetBuilder withComponentId(String id) {
        this.componentId = id;
        return this;
    }

    public Asset build() {
        return new Asset().setId(id).setName(name).setType(type).setScope(scope).setOrder(order).setComponentId(componentId);
    }

}
