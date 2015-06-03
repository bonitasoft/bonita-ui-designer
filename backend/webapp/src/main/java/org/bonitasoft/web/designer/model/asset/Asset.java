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
package org.bonitasoft.web.designer.model.asset;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.hibernate.validator.constraints.NotBlank;

/**
 * A web resource can be attached to a component
 */
public class Asset {

    public interface JsonViewAsset {
    }

    /**
     * An asset is identified by its name
     */
    @NotBlank(message = "Asset name should not be blank")
    @CheckAssetName(message = "Asset name should be a filename containing only alphanumeric characters and no space or an external URL")
    private String name;
    /**
     * AssetType correspond to the file type
     */
    @NotNull(message = "Asset type may not be null")
    private AssetType type;
    /**
     * If asset is linked to a widget, we need the id. When a widget is in a page we must have
     * an id to find the widget and after the asset
     */
    private String componentId;
    /**
     * An asset can belong to a widget or a page
     */
    private AssetScope scope;
    /**
     * Asset order is important. User or the system has to define it
     */
    private int order;
    /**
     * If asset is desactivated this value is true
     */
    private boolean inactive;

    @JsonIgnore
    public boolean isExternal() {
        return name != null && name.startsWith("http");
    }

    @JsonView({JsonViewPersistence.class, JsonViewAsset.class})
    public String getName() {
        return name;
    }

    public Asset setName(String name) {
        this.name = name;
        return this;
    }

    @JsonView({JsonViewPersistence.class, JsonViewAsset.class})
    public AssetType getType() {
        return type;
    }

    public Asset setType(AssetType type) {
        this.type = type;
        return this;
    }

    @JsonView({JsonViewAsset.class})
    public String getComponentId() {
        return componentId;
    }

    public Asset setComponentId(String componentId) {
        this.componentId = componentId;
        return this;
    }

    @JsonView({JsonViewAsset.class})
    public AssetScope getScope() {
        return scope;
    }

    public Asset setScope(AssetScope scope) {
        this.scope = scope;
        return this;
    }

    @JsonView({JsonViewPersistence.class, JsonViewAsset.class})
    public int getOrder() {
        return order;
    }

    public Asset setOrder(int order) {
        this.order = order;
        return this;
    }

    @JsonView({JsonViewPersistence.class, JsonViewAsset.class})
    public boolean isInactive() {
        return inactive;
    }

    public Asset setInactive(boolean inactive) {
        this.inactive = inactive;
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        //componentId is not in hashcode. If a page use a widget asset with the same name
        //the page asset must to be used
        if (obj instanceof Asset) {
            final Asset other = (Asset) obj;
            return new EqualsBuilder()
                    .append(name, other.name)
                    .append(type, other.type)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        //componentId is not in hashcode. If a page use a widget asset with the same name
        //the page asset must to be used
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(type)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("type", type)
                .append("componentId", componentId)
                .toString();
    }

}