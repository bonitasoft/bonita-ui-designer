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

import java.nio.file.Path;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.hibernate.validator.constraints.NotBlank;

/**
 * A web resource can be attached to a component
 */
public class Asset<T extends Identifiable> {

    @NotBlank(message = "Asset name should not be blank")
    @Pattern(regexp = "[a-zA-Z0-9._-]*$", message = "Asset name should contains only alphanumeric characters, with no space")
    private String name;
    @NotNull(message = "Asset type may not be null")
    private AssetType type;
    @NotNull(message = "Asset has be attached to a component")
    private T component;

    @JsonIgnore
    public T getComponent() {
        return component;
    }

    public Asset setComponent(T component) {
        this.component = component;
        return this;
    }

    @JsonView({JsonViewPersistence.class})
    public String getName() {
        return name;
    }

    public Asset setName(String name) {
        this.name = name;
        return this;
    }

    @JsonView({JsonViewPersistence.class})
    public AssetType getType() {
        return type;
    }

    public Asset setType(AssetType type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
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
                .toString();
    }

}
