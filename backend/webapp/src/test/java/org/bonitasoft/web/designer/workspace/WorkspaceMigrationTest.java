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

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import javax.inject.Inject;

import com.google.common.base.Function;
import org.bonitasoft.web.designer.ApplicationConfig;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
@WebAppConfiguration("file:target/test-classes")
public class WorkspaceMigrationTest {

    @Value("${designer.version}")
    String version;

    @Inject
    WorkspaceInitializer workspaceInitializer;

    @Inject
    PageRepository pageRepository;

    @Inject
    WidgetRepository widgetRepository;

    String PAGE_HIGHER_MIGRATION_VERSION = "1.10.18";

    String WIDGET_HIGHER_MIGRATION_VERSION = "1.10.12";

    @Before
    public void setUp() throws Exception {
        workspaceInitializer.contextInitialized();
        workspaceInitializer.migrateWorkspace();
    }

    @Test
    public void should_migrate_a_page() {

        Page page = pageRepository.get("page_1_0_0");

        assertThat(page.getDesignerVersion()).isEqualTo(PAGE_HIGHER_MIGRATION_VERSION);
        assertThat(page.getPreviousDesignerVersion()).isEqualTo("1.0.0");

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

        assertThat(page.getDesignerVersion()).isEqualTo(PAGE_HIGHER_MIGRATION_VERSION);
        Map<String, PropertyValue> propertyValues = concat(page.getRows()).iterator().next().getPropertyValues();
        PropertyValue allowHTMLProperty = propertyValues.get("allowHTML");

        assertThat(allowHTMLProperty).isNotNull();
        assertThat(allowHTMLProperty.getType()).isEqualTo(BondType.CONSTANT.toJson());
        assertThat(allowHTMLProperty.getValue()).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void should_migrate_a_widget() {

        Widget widget = widgetRepository.get("widget_1_0_0");

        assertThat(widget.getDesignerVersion()).isEqualTo(WIDGET_HIGHER_MIGRATION_VERSION);
        assertThat(widget.getPreviousDesignerVersion()).isEqualTo("1.0.0");

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

        assertThat(page.getDesignerVersion()).isEqualTo(PAGE_HIGHER_MIGRATION_VERSION);
        assertThat(page.getPreviousDesignerVersion()).isEqualTo("1.0.1");

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

        assertThat(page.getDesignerVersion()).isEqualTo(PAGE_HIGHER_MIGRATION_VERSION);
        assertThat(page.getPreviousDesignerVersion()).isEqualTo("1.0.1");

        assertThat(transform(page.getAssets(), new Function<Asset, String>() {

            @Override
            public String apply(Asset asset) {
                return asset.getName();
            }
        })).doesNotContain(UIBootstrapAssetMigrationStep.ASSET_FILE_NAME).hasSize(1);
    }
}
