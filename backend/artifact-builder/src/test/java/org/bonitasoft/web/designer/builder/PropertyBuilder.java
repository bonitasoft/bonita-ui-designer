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

import org.bonitasoft.web.designer.model.widget.BondType;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.PropertyType;

public class PropertyBuilder {

    private String label = "aLabel";
    private String name = "aName";
    private String caption = "the caption";
    private String help = "the help";
    private PropertyType type = PropertyType.TEXT;
    private BondType bond = BondType.CONSTANT;

    public static PropertyBuilder aProperty() {
        return new PropertyBuilder();
    }

    public PropertyBuilder name(String name) {
        this.name = name;
        return this;
    }

    public PropertyBuilder label(String label) {
        this.label = label;
        return this;
    }

    public PropertyBuilder bond(BondType bond) {
        this.bond = bond;
        return this;
    }

    public Property build() {
        Property p = new Property();
        p.setLabel(label);
        p.setName(name);
        p.setCaption(caption);
        p.setType(type);
        p.setHelp(help);
        p.setBond(bond);
        return p;
    }
}
