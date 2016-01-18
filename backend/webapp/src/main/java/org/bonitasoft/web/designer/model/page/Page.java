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

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.ElementContainer;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.visitor.ElementVisitor;
import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.Instant;

public class Page extends DesignerArtifact implements Previewable, Identifiable, ElementContainer, Assetable {

    private String id;
    @NotBlank(message = "Page name should not be blank")
    @Pattern(regexp = "[a-zA-Z0-9]*$", message = "Page name should contains only alphanumeric characters with no space")
    private String name;
    private String type = "page";
    private Instant lastUpdate;
    private List<List<Element>> rows = new ArrayList<>();
    private Set<Asset> assets = new HashSet<>();
    private Set<String> inactiveAssets = new HashSet<>();
    private Map<String, Data> data = new HashMap<>();

    @JsonView({ JsonViewLight.class, JsonViewPersistence.class })
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonView({ JsonViewLight.class, JsonViewPersistence.class })
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonView({ JsonViewLight.class, JsonViewPersistence.class })
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonView({ JsonViewLight.class, JsonViewPersistence.class })
    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @JsonView({ JsonViewPersistence.class })
    @Override
    public List<List<Element>> getRows() {
        return rows;
    }

    public void setRows(List<List<Element>> rows) {
        this.rows = rows;
    }

    @JsonView({ JsonViewPersistence.class })
    public Set<Asset> getAssets() {
        return assets;
    }

    @Override
    public void addAsset(Asset asset) {
        if (assets == null) {
            assets = new HashSet<>();
        }
        assets.add(asset);
    }

    @Override
    public void addAssets(Set<Asset> assets) {
        if (this.assets == null) {
            this.assets = new HashSet<>();
        }
        this.assets.addAll(assets);
    }

    public void setAssets(Set<Asset> assets) {
        this.assets = assets;
    }

    @JsonView({ JsonViewPersistence.class })
    public Set<String> getInactiveAssets() {
        return inactiveAssets;
    }

    public void setInactiveAssets(Set<String> inactiveAssets) {
        this.inactiveAssets = inactiveAssets;
    }

    @JsonView({ JsonViewPersistence.class })
    public Map<String, Data> getData() {
        return data;
    }

    public void setData(Map<String, Data> data) {
        this.data = data;
    }

    @Override
    public void addData(String name, Data value) {
        data.put(name, value);
    }

    @Override
    public void removeData(String dataName) throws NotFoundException {
        if (!data.containsKey(dataName)) {
            throw new NotFoundException("Data [" + dataName + "] doesn't exists for page [" + id + "]");
        }
        data.remove(dataName);
    }

    /**
     * Visits all the elements of this page with the given element visitor
     */
    public <T> T accept(ElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Page) {
            final Page other = (Page) obj;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(rows, other.rows)
                    .append(data, other.data)
                    .append(getDesignerVersion(), other.getDesignerVersion())
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(rows)
                .append(data)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("rows", rows)
                .append("data", data)
                .append("version", getDesignerVersion())
                .toString();
    }
}
