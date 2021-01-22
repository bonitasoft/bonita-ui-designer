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

package org.bonitasoft.web.designer.workspace;

import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Function;
import org.bonitasoft.web.designer.Main;
import org.bonitasoft.web.designer.migration.page.UIBootstrapAssetMigrationStep;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.widget.BondType;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class WorkspaceMigrationTest {

    @Value("${designer.version}")
    private String uidVersion;

    @Value("${designer.modelVersion}")
    private String modelVersion;

    @Inject
    private WorkspaceInitializer workspaceInitializer;

    @Inject
    private PageRepository pageRepository;

    @Inject
    private WidgetRepository widgetRepository;

    private String PAGE_HIGHER_MIGRATION_VERSION = "2.1";

    private String WIDGET_HIGHER_MIGRATION_VERSION = "2.1";

    @Before
    public void setUp() throws Exception {
        workspaceInitializer.contextInitialized();
        workspaceInitializer.migrateWorkspace();
    }

    @Test
    public void should_migrate_a_page() {

        Page page = pageRepository.get("page_1_0_0");

        assertThat(page.getArtifactVersion()).isEqualTo(PAGE_HIGHER_MIGRATION_VERSION);
        assertThat(page.getPreviousArtifactVersion()).isEqualTo("1.0.0");

        assertThat(transform(page.getAssets(), new Function<Asset, String>() {

            @Override
            public String apply(Asset asset) {
                return asset.getId();
            }
        })).doesNotContainNull();
    }

    @Test
    public void should_migrate_a_page_property_values() {

        Page page = pageRepository.get("page_1_0_1");

        Map<String, PropertyValue> propertyValues = concat(page.getRows()).iterator().next().getPropertyValues();

        assertThat(transform(propertyValues.values(), new Function<PropertyValue, String>() {

            @Override
            public String apply(PropertyValue value) {
                return value.getType();
            }
        })).doesNotContain("data");
    }

    @Test
    public void should_migrate_a_page_adding_text_widget_interpret_HTML_property_value() {

        Page page = pageRepository.get("page_1_5_51");

        assertThat(page.getArtifactVersion()).isEqualTo(PAGE_HIGHER_MIGRATION_VERSION);
        Map<String, PropertyValue> propertyValues = concat(page.getRows()).iterator().next().getPropertyValues();
        PropertyValue allowHTMLProperty = propertyValues.get("allowHTML");

        assertThat(allowHTMLProperty).isNotNull();
        assertThat(allowHTMLProperty.getType()).isEqualTo(BondType.CONSTANT.toJson());
        assertThat(allowHTMLProperty.getValue()).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void should_migrate_a_widget() {

        Widget widget = widgetRepository.get("widget_1_0_0");

        assertThat(widget.getArtifactVersion()).isEqualTo(WIDGET_HIGHER_MIGRATION_VERSION);
        assertThat(widget.getPreviousArtifactVersion()).isEqualTo("1.0.0");

        assertThat(transform(widget.getAssets(), new Function<Asset, String>() {

            @Override
            public String apply(Asset asset) {
                return asset.getId();
            }
        })).doesNotContainNull();
    }

    @Test
    public void should_migrate_a_page_adding_uiBootstrap() {

        Page page = pageRepository.get("page_1_0_1");

        assertThat(transform(page.getAssets(), new Function<Asset, String>() {

            @Override
            public String apply(Asset asset) {
                return asset.getName();
            }
        })).contains(UIBootstrapAssetMigrationStep.ASSET_FILE_NAME);
    }

    @Test
    public void should_migrate_a_page_not_adding_uiBootstrap_when_already_a_page_asset() {

        Page page = pageRepository.get("pageWithUIBootstrap");

        assertThat(page.getArtifactVersion()).isEqualTo(PAGE_HIGHER_MIGRATION_VERSION);
        assertThat(page.getPreviousArtifactVersion()).isEqualTo("1.0.1");

        assertThat(transform(page.getAssets(), new Function<Asset, String>() {

            @Override
            public String apply(Asset asset) {
                return asset.getName();
            }
        })).doesNotContain(UIBootstrapAssetMigrationStep.ASSET_FILE_NAME).hasSize(2);
    }

    @Test
    public void should_migrate_a_page_not_adding_uiBootstrap_when_already_a_widget_asset() {

        Page page = pageRepository.get("pageWithUIBootstrapWidget");

        assertThat(page.getArtifactVersion()).isEqualTo(PAGE_HIGHER_MIGRATION_VERSION);
        assertThat(page.getPreviousArtifactVersion()).isEqualTo("1.0.1");

        assertThat(transform(page.getAssets(), new Function<Asset, String>() {

            @Override
            public String apply(Asset asset) {
                return asset.getName();
            }
        })).doesNotContain(UIBootstrapAssetMigrationStep.ASSET_FILE_NAME).hasSize(1);
    }
}
