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
package org.bonitasoft.web.designer.retrocompatibility;

import java.util.UUID;
import javax.inject.Named;

import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.Versioned;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.repository.AbstractRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * @see org.bonitasoft.web.designer.retrocompatibility.JacksonRetrocompatibilityHandler
 */
@Named
public class ComponentMigrator {

    protected static final Logger logger = LoggerFactory.getLogger(ComponentMigrator.class);

    @Value("${designer.version}")
    private String version;

    //Asset changes in version 1.0.1 (UUID is added)
    private String formatAssetMigrationUiid = "1.0.2";

    public <T extends Identifiable, U extends Versioned & Assetable> void migrate(Repository<T> repository, T component) {
        if (component instanceof Assetable) {
            String vers = component.getDesignerVersion();
            if(formatAssetMigrationUiid.compareTo(vers) > 0) {
                migrateAsset((U) component);
            }
            repository.save(component);
        }
    }

    private <T extends Versioned & Assetable<T>> void migrateAsset(T component) {
        logger.info(String.format("The page [%s] id=[%s] is in version %s... it need to migrate to the version %s",
                component.getName(), component.getId(), component.getDesignerVersion(), version));

        //Asset changes in version 1.0.1-* (UUID is added)
        component.setDesignerVersion(version);
        for (Asset asset : component.getAssets()) {
            if (asset.getId() == null) {
                asset.setId(UUID.randomUUID().toString());
                logger.info(String.format("An uuid %s has been added to asset %s (Id was introduced in 1.0.0)", asset.getId(), asset.getName()));
            }
        }
    }
}
