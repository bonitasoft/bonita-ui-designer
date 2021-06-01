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
package org.bonitasoft.web.designer.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bonitasoft.web.designer.model.asset.Asset;

import java.util.Set;

public interface Assetable extends Identifiable {

    Set<Asset> getAssets();

    void setAssets(Set<Asset> assets);

    void addAsset(Asset asset);

    void addAssets(Set<Asset> assets);

    /**
     * Return the max order of the assets for the current component
     */
    @JsonIgnore
    default int getNextAssetOrder() {
        var order = 0;
        for (var asset : getAssets()) {
            order = asset.getOrder() > order ? asset.getOrder() : order;
        }
        return order + 1;
    }
}
