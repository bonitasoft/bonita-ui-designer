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
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.*;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.*;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.bonitasoft.web.designer.model.asset.AssetType.CSS;

public abstract class AbstractPage extends DesignerArtifact implements Previewable, Identifiable, ElementContainer, Assetable {

    private final Map<String, Data> data = null;
    private String id;
    @NotBlank(message = "Page name should not be blank")
    @Pattern(regexp = "[a-zA-Z0-9]*$", message = "Page name should contains only alphanumeric characters with no space")
    private String name;
    private Instant lastUpdate;
    private List<List<Element>> rows = new ArrayList<>();
    private Set<Asset> assets = new HashSet<>();
    private Set<String> inactiveAssets = new HashSet<>();
    private Map<String, Variable> variables = new HashMap<>();
    private boolean hasValidationError = false;

    private Set<WebResource> automaticWebResources = new HashSet<>();

    @JsonView({JsonViewLight.class, JsonViewPersistence.class})
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonView({JsonViewLight.class, JsonViewPersistence.class})
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonView({JsonViewLight.class, JsonViewPersistence.class})
    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @JsonView({JsonViewPersistence.class})
    @Override
    public List<List<Element>> getRows() {
        return rows;
    }

    public void setRows(List<List<Element>> rows) {
        this.rows = rows;
    }

    @JsonView({JsonViewPersistence.class})
    public Set<Asset> getAssets() {
        return assets;
    }

    public void setAssets(Set<Asset> assets) {
        this.assets = assets;
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

    @JsonView({JsonViewPersistence.class})
    public Set<String> getInactiveAssets() {
        return inactiveAssets;
    }

    public void setInactiveAssets(Set<String> inactiveAssets) {
        this.inactiveAssets = inactiveAssets;
    }

    public boolean hasAsset(final AssetType type, final String name) {
        return assets.stream().anyMatch(asset -> CSS.equals(type) && asset.getName().equals(name));
    }

    public Set<WebResource> getAutomaticWebResources() {
        return this.automaticWebResources;
    }

    @JsonIgnore
    public void setAutomaticWebResources(Set<WebResource> automaticWebResources) {
        this.automaticWebResources = automaticWebResources;
    }

    @Deprecated
    @JsonView({JsonViewPersistence.class})
    public Map<String, Data> getData() {
        return null;
    }

    @Deprecated
    public void setData(Map<String, Data> data) {
        if (data != null) {
            for (Map.Entry<String, Data> dataEntry : data.entrySet()) {
                variables.put(dataEntry.getKey(), convertDataToVariable(dataEntry.getValue()));
            }
        }
    }

    private Variable convertDataToVariable(Data data) {
        var variableValue = Objects.toString(data.getValue(), null);
        var variable = new Variable(data.getType(), variableValue);
        variable.setExposed(data.isExposed());
        return variable;
    }

    @Deprecated
    public void addData(String name, Data value) {
        variables.put(name, convertDataToVariable(value));
    }

    @JsonView({JsonViewPersistence.class})
    public Map<String, Variable> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Variable> variables) {
        this.variables = variables;
    }

    @Override
    public void addVariable(String name, Variable value) {
        variables.put(name, value);
    }

    @Override
    public void removeVariable(String variableName) throws NotFoundException {
        if (!variables.containsKey(variableName)) {
            throw new NotFoundException("Variable [" + variableName + "] doesn't exists for page [" + id + "]");
        }
        variables.remove(variableName);
    }

    /**
     * Visits all the elements of this page with the given element visitor
     */
    public <T> T accept(ElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof AbstractPage) {
            final AbstractPage other = (AbstractPage) obj;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(rows, other.rows)
                    .append(data, other.data)
                    .append(getModelVersion(), other.getModelVersion())
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
                .append("modelVersion", getModelVersion())
                .append("designerVersion", getDesignerVersion())
                .toString();
    }

    @JsonView({JsonViewLight.class, JsonViewPersistence.class})
    public boolean getHasValidationError() {
        return hasValidationError;
    }

    public void setHasValidationError(boolean hasValidationError) {
        this.hasValidationError = hasValidationError;
    }
}
