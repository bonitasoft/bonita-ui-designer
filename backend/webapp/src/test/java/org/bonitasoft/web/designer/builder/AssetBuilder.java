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

import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;

public class AssetBuilder {

    private String name = "maresource.js";
    private AssetType type = AssetType.JAVASCRIPT;
    private Identifiable component;

    public static AssetBuilder anAsset() {
        return new AssetBuilder();
    }

    public static Asset<Page> aFilledAsset() {
        return anAsset().withPage(aPage().build()).buildPageAsset();
    }

    public AssetBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AssetBuilder withType(AssetType type) {
        this.type = type;
        return this;
    }

    public AssetBuilder withPage(Page page) {
        this.component = page;
        return this;
    }

    public Asset<Page> buildPageAsset() {
        Asset<Page> asset = new Asset<>();
        asset.setName(name);
        asset.setType(type);
        asset.setComponent((Page) component);
        return asset;
    }
}
