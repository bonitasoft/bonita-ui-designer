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

import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aParagraph;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.anInput;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FragmentElementBuilder.aFragmentElement;
import static org.bonitasoft.web.designer.builder.RowBuilder.aRow;
import static org.bonitasoft.web.designer.builder.VariableBuilder.aConstantVariable;

public class FragmentBuilder {

    private String id = UUID.randomUUID().toString();
    private String name = "fragment";
    private String designerVersion;
    private String modelVersion;
    private String previousArtifactVersion;
    private String previousDesignerVersion;
    private Map<String, Variable> variables = new HashMap<>();
    private List<List<Element>> rows = new ArrayList<>();
    private Set<Asset> assets = new HashSet<>();
    private boolean favorite = false;
    private boolean hasValidationError = false;
    private MigrationStatusReport migrationStatusReport = new MigrationStatusReport();

    public static FragmentBuilder aFragment() {
        return new FragmentBuilder();
    }

    public FragmentBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public FragmentBuilder with(RowBuilder row) {
        rows.add(row.build());
        return this;
    }

    public FragmentBuilder with(Element... elements) {
        rows.add(asList(elements));
        return this;
    }

    public FragmentBuilder with(ElementBuilder... elements) {
        for (ElementBuilder element : elements) {
            rows.add(asList(element.build()));
        }
        return this;
    }

    public FragmentBuilder with(AssetBuilder... assets) {
        for (AssetBuilder asset : assets) {
            this.assets.add(asset.build());
        }
        return this;
    }

    public FragmentBuilder with(Asset... assets) {
        for (Asset asset : assets) {
            this.assets.add(asset);
        }
        return this;
    }

    public FragmentBuilder with(Fragment fragment) {
        return with(aFragmentElement().withFragmentId(fragment.getId()));
    }

    public FragmentBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public FragmentBuilder withDesignerVersion(String version) {
        this.designerVersion = version;
        return this;
    }

    public FragmentBuilder withModelVersion(String version) {
        this.modelVersion = version;
        return this;
    }

    public FragmentBuilder withPreviousArtifactVersion(String previousArtifactVersion) {
        this.previousArtifactVersion = previousArtifactVersion;
        return this;
    }

    public FragmentBuilder withPreviousDesignerVersion(String previousDesignerVersion) {
        this.previousDesignerVersion = previousDesignerVersion;
        return this;
    }

    public FragmentBuilder favorite() {
        this.favorite = true;
        return this;
    }

    public FragmentBuilder notFavorite() {
        this.favorite = false;
        return this;
    }

    public FragmentBuilder withMigrationStatusReport(MigrationStatusReport migrationStatusReport) {
        this.migrationStatusReport = migrationStatusReport;
        return this;
    }

    public FragmentBuilder isCompatible(boolean compatible) {
        this.migrationStatusReport.setCompatible(compatible);
        return this;
    }

    public FragmentBuilder isMigration(boolean migration) {
        this.migrationStatusReport.setMigration(migration);
        return this;
    }

    public FragmentBuilder withHasValidationError(boolean hasValidationError) {
        this.hasValidationError = hasValidationError;
        return this;
    }

    public Fragment build() {
        Fragment fragment = new Fragment();
        fragment.setId(id);
        fragment.setName(name);
        fragment.setVariables(variables);
        fragment.setRows(rows);
        fragment.setAssets(assets);
        fragment.setFavorite(favorite);
        fragment.setDesignerVersion(designerVersion);
        fragment.setModelVersion(modelVersion);
        if (previousArtifactVersion != null) {
            fragment.setPreviousArtifactVersion(previousArtifactVersion);
        } else {
            fragment.setPreviousDesignerVersion(previousDesignerVersion);
        }
        fragment.setStatus(migrationStatusReport);

        fragment.setHasValidationError(hasValidationError);
        return fragment;
    }

    public FragmentBuilder withVariable(String name, Variable variable) {
        this.variables.put(name, variable);
        return this;
    }

    public FragmentBuilder withVariable(String name, VariableBuilder variableBuilder) {
        return withVariable(name, variableBuilder.build());
    }

    public static Fragment aFilledFragment(String id) throws Exception {
        RowBuilder row = aRow().with(
                aParagraph().withPropertyValue("content", "hello <br/> world").withDimensions(ResponsiveDimension.md(6)),
                anInput().withPropertyValue("required", false).withPropertyValue("placeholder", "enter you're name").withDimensions(ResponsiveDimension.md(6)));

        Container containerWithTwoRows = aContainer().with(row, row).build();

        TabContainer tabContainer = new TabContainer();
        tabContainer.setContainer(containerWithTwoRows);

        TabContainer tabContainer2 = new TabContainer();
        tabContainer2.setContainer(containerWithTwoRows);

        TabsContainer tabsContainer = new TabsContainer();
        tabsContainer.setTabList(asList(tabContainer, tabContainer2));

        FragmentElement fragment = new FragmentElement();
        fragment.setId("a-fragment");
        fragment.setDimension(Map.of("md", 8));
        Container fragmentContainer = aContainer().with(fragment).build();

        return aFragment().withId(id).with(tabsContainer, containerWithTwoRows, fragmentContainer)
                .withVariable("aVariable", aConstantVariable().value("a value"))
                .withVariable("anotherVariable", aConstantVariable().value("4"))
                .build();
    }
}
