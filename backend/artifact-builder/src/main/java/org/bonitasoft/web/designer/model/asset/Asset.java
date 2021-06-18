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

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Comparator;

import static java.util.Comparator.comparingInt;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * A web resource can be attached to a component
 */
public class Asset {

    private String id;
    /**
     * An asset is identified by its name
     */
    @NotBlank(message = "Asset name should not be blank")
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
    private String scope;
    /**
     * Asset order is important. User or the system has to define it
     */
    private int order;
    /**
     * If asset is desactivated this value is false
     */
    private boolean active = true;
    private boolean external = false;

    public static Comparator<Asset> getComparatorByOrder() {
        return comparingInt(Asset::getOrder);
    }

    public static Comparator<Asset> getComparatorByComponentId() {
        return (asset1, asset2) -> ObjectUtils.compare(asset1.getComponentId(), asset2.getComponentId(), true);
    }

    @JsonView({JsonViewAsset.class, JsonViewPersistence.class})
    public String getId() {
        return id;
    }

    public Asset setId(String id) {
        this.id = id;
        return this;
    }

    @JsonView({JsonViewAsset.class, JsonViewPersistence.class})
    public boolean isExternal() {
        return external;
    }

    public Asset setExternal(boolean external) {
        this.external = external;
        return this;
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
    public String getScope() {
        return scope;
    }

    public Asset setScope(String scope) {
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

    @JsonView({JsonViewAsset.class})
    public boolean isActive() {
        return active;
    }

    public Asset setActive(boolean inactive) {
        this.active = inactive;
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Asset) {
            final Asset other = (Asset) obj;
            return new EqualsBuilder()
                    .append(name, other.name)
                    .append(type, other.type)
                    .append(componentId, other.componentId)
                    .isEquals();
        } else {
            return false;
        }
    }

    public boolean equalsWithoutComponentId(final Object obj) {
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
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("type", type)
                .append("componentId", componentId)
                .append("active", active)
                .toString();
    }

    public interface JsonViewAsset {
    }

}
