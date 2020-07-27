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

package org.bonitasoft.web.designer.controller;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.bonitasoft.web.designer.model.JsonViewLight;

public class MigrationStatusReport {
    private boolean compatible;
    private boolean migration;

    public MigrationStatusReport(boolean compatible, boolean migration) {
        this.compatible = compatible;
        this.migration = migration;
    }

    public MigrationStatusReport() {
        this.compatible = true;
        this.migration = true;
    }

    @JsonView({ JsonViewLight.class })
    public boolean isCompatible() {
        return compatible;
    }

    @JsonIgnore
    public void setCompatible(boolean compatible) {
        this.compatible = compatible;
    }

    @JsonView({ JsonViewLight.class })
    public boolean isMigration() {
        return migration;
    }

    @JsonIgnore
    public void setMigration(boolean migration) {
        this.migration = migration;
    }

    @Override
    public String toString() {
        return "{" +
                "\"compatible\":" + compatible +
                ",\"migration\":" + migration +
                '}';
    }
}
