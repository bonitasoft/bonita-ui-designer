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

import static java.util.Arrays.asList;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aParagraph;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.anInput;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.DataBuilder.aConstantData;
import static org.bonitasoft.web.designer.builder.RowBuilder.aRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.Tab;
import org.bonitasoft.web.designer.model.page.TabsContainer;

public class PageBuilder {

    private List<List<Element>> rows = new ArrayList<>();
    private Map<String, Data> data = new HashMap<>();
    private String name = "pageName";
    private String id;

    private PageBuilder() {
    }

    public static PageBuilder aPage() {
        return new PageBuilder();
    }

    public PageBuilder with(Element... elements) {
        rows.add(asList(elements));
        return this;
    }

    public PageBuilder with(ElementBuilder element) {
        rows.add(asList(element.build()));
        return this;
    }



    public PageBuilder withData(String name, Data data) {
        this.data.put(name, data);
        return this;
    }

    public PageBuilder withData(String name, DataBuilder data) {
        return withData(name, data.build());
    }

    public PageBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public PageBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public Page build() {
        Page page = new Page();
        page.setName(name);
        page.setRows(rows);
        page.setData(data);
        page.setId(id);
        return page;
    }

    public static Page aFilledPage(String id) throws Exception {
        RowBuilder row = aRow().with(
                aParagraph().withPropertyValue("content", "hello <br/> world").withDimension(6),
                anInput().withPropertyValue("required", false).withPropertyValue("placeholder", "enter you're name").withDimension(6));

        Container containerWithTwoRows = aContainer().with(row, row).build();

        Tab tab = new Tab();
        tab.setTitle("tab1");
        tab.setContainer(containerWithTwoRows);

        Tab tab2 = new Tab();
        tab.setTitle("tab2");
        tab.setContainer(containerWithTwoRows);

        TabsContainer tabsContainer = new TabsContainer();
        tabsContainer.setTabs(asList(tab, tab2));

        FormContainer formContainer = new FormContainer();
        formContainer.setContainer(aContainer().with(aParagraph().withPropertyValue("content", "hello <br/> world").withDimension(6)).build());

        return aPage().withId(id).with(tabsContainer, containerWithTwoRows, formContainer)
                .withData("aData", aConstantData().value("a value"))
                .withData("anotherData", aConstantData().value(4))
                .build();
    }
}
