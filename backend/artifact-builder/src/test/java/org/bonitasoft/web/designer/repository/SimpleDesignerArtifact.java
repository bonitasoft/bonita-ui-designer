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
package org.bonitasoft.web.designer.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.JsonViewMetadata;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import java.time.Instant;

@JsonIgnoreProperties({"type"})
public final class SimpleDesignerArtifact extends DesignerArtifact {
    private String id;
    private String name;
    private int number;
    private SimpleDesignerArtifact another;
    private String metadata;

    public SimpleDesignerArtifact() {
    }

    public SimpleDesignerArtifact(String id, String name, int number) {
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

    public SimpleDesignerArtifact getAnother() {
        return another;
    }

    public void setAnother(SimpleDesignerArtifact another) {
        this.another = another;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @JsonView(JsonViewMetadata.class)
    public String getMetadata() {
        return metadata;
    }

    @JsonProperty("type")
    public String getType() {
        return "simple";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof SimpleDesignerArtifact) {
            final SimpleDesignerArtifact other = (SimpleDesignerArtifact) obj;
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
