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
package org.bonitasoft.web.designer.rendering;

import java.io.IOException;

import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

/**
 * Helper used to display the asset src
 */
public class AssetHelper implements Helper<Object> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<Object> INSTANCE = new AssetHelper();

    @Override
    public CharSequence apply(final Object context, final Options options)
            throws IOException {

        if (context instanceof Asset) {
            Asset asset = (Asset) context;
            String widgetPrefix = "";

            if (asset.isExternal()) {
                return asset.getName();
            }
            if (AssetScope.WIDGET.equals(asset.getScope())) {
                widgetPrefix = String.format("widgets/%s/", asset.getComponentId());
            }
            return String.format("%sassets/%s/%s", widgetPrefix, asset.getType().getPrefix(), asset.getName());
        }
        return null;
    }

}
