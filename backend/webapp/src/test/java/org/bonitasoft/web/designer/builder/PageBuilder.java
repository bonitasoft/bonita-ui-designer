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

import com.google.common.base.Function;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aParagraph;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.anInput;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.VariableBuilder.aConstantVariable;
import static org.bonitasoft.web.designer.builder.RowBuilder.aRow;

import java.util.*;

public class PageBuilder {

    private List<List<Element>> rows = new ArrayList<>();
    private Set<Asset> assets = new HashSet<>();
    private Set<String> inactiveAssets = new HashSet<>();
    @Deprecated
    private Map<String, Data> data = null;
    private Map<String, Variable> variables = new HashMap<>();
    private String name = "pageName";
    private String id = UUID.randomUUID().toString();
    private String uuid = UUID.randomUUID().toString();
    private String version;
    private String previousDesignerVersion;
    private boolean favorite = false;
    private String type;

    private PageBuilder() {
    }

    public static PageBuilder aPage() {
        return new PageBuilder();
    }

    public PageBuilder with(Element... elements) {
        rows.add(asList(elements));
        return this;
    }

    public PageBuilder with(ElementBuilder... elements) {
        rows.add(newArrayList(transform(asList(elements), new Function<ElementBuilder, Element>() {
            @Override
            public Element apply(ElementBuilder elementBuilder) {
                return elementBuilder.build();
            }
        })));
        return this;
    }

    public PageBuilder withAsset(Asset... assets) {
        for (Asset asset : assets) {
            this.assets.add(asset);
        }
        return this;
    }

    public PageBuilder withAsset(AssetBuilder... assets) {
        for (AssetBuilder asset : assets) {
            this.assets.add(asset.build());
        }
        return this;
    }

    public PageBuilder withInactiveAsset(String... assetIds) {
        for (String id : assetIds) {
            this.inactiveAssets.add(id);
        }
        return this;
    }

    @Deprecated
    public PageBuilder withData(String name, Data data) {
        variables.put(name, convertDataToVariable(data));
        return this;
    }

    private Variable convertDataToVariable(Data data) {
        String variableValue = Objects.toString(data.getValue(), null);
        Variable variable = new Variable(data.getType(), variableValue);
        variable.setExposed(data.isExposed());
        return variable;
    }
    
    @Deprecated
    public PageBuilder withData(String name, DataBuilder data) {
        return withData(name, data.build());
    }

    public PageBuilder withVariable(String name, Variable variable) {
        this.variables.put(name, variable);
        return this;
    }

    public PageBuilder withVariable(String name, VariableBuilder variableBuilder) {
        return withVariable(name, variableBuilder.build());
    }

    public PageBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public PageBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public PageBuilder withUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public PageBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public PageBuilder withPreviousDesignerVersion(String previousDesignerVersion) {
        this.previousDesignerVersion = previousDesignerVersion;
        return this;
    }

    public PageBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public PageBuilder favorite() {
        this.favorite = true;
        return this;
    }

    public PageBuilder notFavorite() {
        this.favorite = false;
        return this;
    }


    public Page build() {
        Page page = new Page();
        page.setName(name);
        page.setRows(rows);
        page.setData(data);
        page.setVariables(variables);
        page.setId(id);
        page.setUUID(uuid);
        page.setAssets(assets);
        page.setInactiveAssets(inactiveAssets);
        page.setDesignerVersion(version);
        page.setPreviousDesignerVersion(previousDesignerVersion);
        page.setFavorite(favorite);

        if (type != null) {
            page.setType(type);
        }
        return page;
    }

    public static Page aFilledPage(String id) throws Exception {
        RowBuilder row = aRow().with(
                aParagraph().withPropertyValue("content", "hello <br/> world").withDimension(6),
                anInput().withDescription("A mandatory name input").withPropertyValue("required", false).withPropertyValue("placeholder", "enter you're name").withDimension(6));

        Container containerWithTwoRows = aContainer().with(row, row).build();

        TabContainer tabContainer = new TabContainer();
        tabContainer.setContainer(containerWithTwoRows);

        TabContainer tabContainer2 = new TabContainer();
        tabContainer2.setContainer(containerWithTwoRows);

        TabsContainer tabsContainer = new TabsContainer();
        tabsContainer.setTabList(asList(tabContainer, tabContainer2));

        FormContainer formContainer = new FormContainer();
        formContainer.setContainer(aContainer().with(aParagraph().withPropertyValue("content", "hello <br/> world").withDimension(6)).build());

        return aPage().withId(id).with(tabsContainer, containerWithTwoRows, formContainer)
                .withAsset(anAsset().withName("asset.js").withScope(AssetScope.PAGE).build())
                .withVariable("aVariable", aConstantVariable().value("a value"))
                .withVariable("anotherVariable", aConstantVariable().value("4"))
                .build();
    }
}
