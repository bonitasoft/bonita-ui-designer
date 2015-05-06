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

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.Instant;

public final class SimpleObject extends Versioned {
    private String id;
    private String name;
    private int number;
    private SimpleObject another;

    public SimpleObject() {
    }

    public SimpleObject(String id, String name, int number) {
        this.id = id;
        this.name = name;
        this.number = number;
    }

    @JsonView(JsonViewPersistence.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonView(JsonViewPersistence.class)
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public SimpleObject getAnother() {
        return another;
    }

    public void setAnother(SimpleObject another) {
        this.another = another;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof SimpleObject) {
            final SimpleObject other = (SimpleObject) obj;
            return new EqualsBuilder()
                    .append(name, other.name)
                    .append(number, other.number)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(number)
                .toHashCode();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setLastUpdate(Instant lastUpdate) {

    }
}
