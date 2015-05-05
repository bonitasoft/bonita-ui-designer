/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.builder;

import org.bonitasoft.web.designer.model.SimpleObject;

public class SimpleObjectBuilder {

    private String name = "objectName";
    private String id;
    private SimpleObject newObject;

    private SimpleObjectBuilder() {
    }

    public static SimpleObjectBuilder aSimpleObjectBuilder() {
        return new SimpleObjectBuilder();
    }


    public SimpleObjectBuilder name(String name) {
        this.name = name;
        return this;
    }

    public SimpleObjectBuilder id(String id) {
        this.id = id;
        return this;
    }

    public SimpleObjectBuilder another(SimpleObject newObject) {
        this.newObject = newObject;
        return this;
    }

    public SimpleObject build() {
        SimpleObject myObject = new SimpleObject(id, name, 1);
        myObject.setAnother(newObject);
        return myObject;
    }

    public static SimpleObject aFilledSimpleObject(String id) throws Exception {
        return new SimpleObjectBuilder()
                .id(id)
                .build();
    }
}
