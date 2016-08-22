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

import org.bonitasoft.web.designer.repository.SimpleDesignerArtifact;

public class SimpleObjectBuilder {

    private String name = "objectName";
    private String id;
    private SimpleDesignerArtifact newObject;
    private String metadata;

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

    public SimpleObjectBuilder another(SimpleDesignerArtifact newObject) {
        this.newObject = newObject;
        return this;
    }

    public SimpleObjectBuilder metadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public SimpleDesignerArtifact build() {
        SimpleDesignerArtifact myObject = new SimpleDesignerArtifact(id, name, 1);
        myObject.setAnother(newObject);
        myObject.setMetadata(metadata);
        return myObject;
    }

    public static SimpleDesignerArtifact aFilledSimpleObject(String id) throws Exception {
        return new SimpleObjectBuilder()
                .id(id)
                .build();
    }
}
