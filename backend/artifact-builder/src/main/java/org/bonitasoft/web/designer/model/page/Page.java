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
package org.bonitasoft.web.designer.model.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import org.bonitasoft.web.designer.model.HasUUID;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.JsonViewPersistence;

@JsonPropertyOrder({"id", "name", "displayName","type","uuid", "modelVersion", "previousArtifactVersion", "lastUpdate", "description", "rows", "variables", "assets", "inactiveAssets", "webResources"})
public class Page extends AbstractPage implements HasUUID {

    //useful for the index and studio
    private String uuid;
    private String type = "page";
    private String description = "Page generated with Bonita UI designer";
    private String displayName;

    @JsonView({JsonViewLight.class, JsonViewPersistence.class})
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonView({JsonViewLight.class, JsonViewPersistence.class})
    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }


    @JsonView({JsonViewPersistence.class})
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonView({JsonViewPersistence.class})
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @JsonIgnore
    public boolean isCompatible() {
        return getStatus() == null || getStatus().isCompatible();
    }
}
