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
package org.bonitasoft.web.designer.migration;

import static java.util.Arrays.asList;

import java.util.List;

import javax.annotation.Resource;

import org.bonitasoft.web.designer.migration.page.BondMigrationStep;
import org.bonitasoft.web.designer.migration.page.PageUUIDMigrationStep;
import org.bonitasoft.web.designer.migration.page.TextWidgetInterpretHTMLMigrationStep;
import org.bonitasoft.web.designer.migration.page.UIBootstrapAssetMigrationStep;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.JsonFileBasedLoader;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.bonitasoft.web.designer.visitor.VisitorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationConfig {

    @Bean
    public BondMigrationStep<Page> pageBondMigrationStep(ComponentVisitor componentVisitor,
                                                         WidgetRepository widgetRepository,
                                                         VisitorFactory visitorFactory) {
        return new BondMigrationStep(componentVisitor, widgetRepository, visitorFactory);
    }

    @Bean
    public TextWidgetInterpretHTMLMigrationStep<Page> pageTextWidgetInterpretHTMLMigrationStep(ComponentVisitor componentVisitor) {
        return new TextWidgetInterpretHTMLMigrationStep(componentVisitor);
    }

    @Bean
    public LiveRepositoryUpdate<Page> pageRepositoryLiveUpdate(JsonFileBasedLoader<Page> pageFileBasedLoader,
                                                               PageRepository pageRepository,
                                                               List<Migration<Page>> pageMigrationSteps) {
        return new LiveRepositoryUpdate<>(pageRepository, pageFileBasedLoader, pageMigrationSteps);
    }

    @Bean
    public LiveRepositoryUpdate<Widget> widgetLiveRepositoryUpdate(
            WidgetLoader widgetLoader,
            WidgetRepository widgetRepository,
            List<Migration<Widget>> widgetMigrationSteps) {
        return new LiveRepositoryUpdate<>(widgetRepository, widgetLoader, widgetMigrationSteps);
    }

    @Bean
    public List<Migration<Page>> pageMigrationSteps(
            BondMigrationStep<Page> pageBondMigrationStep,
            StyleAssetMigrationStep styleAssetMigrationStep,
            TextWidgetInterpretHTMLMigrationStep<Page> pageTextWidgetInterpretHTMLMigrationStep,
            UIBootstrapAssetMigrationStep uiBootstrapAssetMigrationStep,
            PageUUIDMigrationStep pageUUIDMigrationStep) {
        return asList(
                new Migration<>("1.0.2", new AssetIdMigrationStep<Page>()),
                new Migration<>("1.0.3", pageBondMigrationStep),
                new Migration<>("1.2.9", new AssetExternalMigrationStep<Page>()),
                new Migration<>("1.5.7", styleAssetMigrationStep),
                new Migration<>("1.5.10", uiBootstrapAssetMigrationStep),
                new Migration<>("1.7.4", pageTextWidgetInterpretHTMLMigrationStep),
                new Migration<>("1.7.25", pageUUIDMigrationStep));
    }


    @Resource
    public List<Migration<Widget>> widgetMigrationSteps() {
        return asList(
                new Migration<>("1.0.2", new AssetIdMigrationStep<Widget>()),
                new Migration<>("1.2.9", new AssetExternalMigrationStep<Widget>())
        );
    }
}
