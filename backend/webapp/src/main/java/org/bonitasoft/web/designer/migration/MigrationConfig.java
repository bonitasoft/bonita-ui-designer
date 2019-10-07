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

import org.bonitasoft.web.designer.config.DesignerConfigConditional;
import org.bonitasoft.web.designer.migration.page.BondMigrationStep;
import org.bonitasoft.web.designer.migration.page.DataToVariableMigrationStep;
import org.bonitasoft.web.designer.migration.page.DynamicTabsContainerMigrationStep;
import org.bonitasoft.web.designer.migration.page.PageUUIDMigrationStep;
import org.bonitasoft.web.designer.migration.page.TableWidgetInterpretHTMLMigrationStep;
import org.bonitasoft.web.designer.migration.page.TableWidgetStylesMigrationStep;
import org.bonitasoft.web.designer.migration.page.TextWidgetInterpretHTMLMigrationStep;
import org.bonitasoft.web.designer.migration.page.TextWidgetLabelMigrationStep;
import org.bonitasoft.web.designer.migration.page.UIBootstrapAssetMigrationStep;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.JsonFileBasedLoader;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.service.PageMigrationApplyer;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetMigrationApplyer;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.bonitasoft.web.designer.visitor.VisitorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(DesignerConfigConditional.class)
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
    public TableWidgetInterpretHTMLMigrationStep<Page> pageTableWidgetInterpretHTMLMigrationStep(ComponentVisitor componentVisitor) {
        return new TableWidgetInterpretHTMLMigrationStep(componentVisitor);
    }
    
    @Bean
    public TableWidgetStylesMigrationStep<Page> pageTableWidgetStylesMigrationStep(ComponentVisitor componentVisitor) {
        return new TableWidgetStylesMigrationStep(componentVisitor);
    }

    @Bean
    public TextWidgetLabelMigrationStep<Page> pageTextWidgetLabelMigrationStep(ComponentVisitor componentVisitor) {
        return new TextWidgetLabelMigrationStep(componentVisitor);
    }

    @Bean
    public LiveRepositoryUpdate<Page> pageRepositoryLiveUpdate(JsonFileBasedLoader<Page> pageFileBasedLoader,
                                                               PageRepository pageRepository) {
        return new LiveRepositoryUpdate<>(pageRepository, pageFileBasedLoader, pageMigrationSteps);
    }

    @Bean
    public DynamicTabsContainerMigrationStep<Page> dynamicTabsContainerMigrationStep() {
        return new DynamicTabsContainerMigrationStep();
    }

    @Resource(name = "pageMigrationStepsList")
    protected List<Migration<Page>> pageMigrationSteps;

    @Bean
    public List<Migration<Page>> pageMigrationStepsList(
            BondMigrationStep<Page> pageBondMigrationStep,
            StyleAssetMigrationStep styleAssetMigrationStep,
            TextWidgetInterpretHTMLMigrationStep<Page> pageTextWidgetInterpretHTMLMigrationStep,
            UIBootstrapAssetMigrationStep uiBootstrapAssetMigrationStep,
            PageUUIDMigrationStep pageUUIDMigrationStep,
            StyleAddModalContainerPropertiesMigrationStep styleAddModalContainerPropertiesMigrationStep,
            TextWidgetLabelMigrationStep<Page> pageTextWidgetLabelMigrationStep,
            DataToVariableMigrationStep<Page> dataToVariableMigrationStep,
            DynamicTabsContainerMigrationStep<Page> dynamicTabsContainerMigrationStep,
            TableWidgetInterpretHTMLMigrationStep<Page> pageTableWidgetInterpretHTMLMigrationStep,
            TableWidgetStylesMigrationStep<Page> pageTableWidgetStylesMigrationStep) {
        return asList(
                new Migration<>("1.0.2", new AssetIdMigrationStep<Page>()),
                new Migration<>("1.0.3", pageBondMigrationStep),
                new Migration<>("1.2.9", new AssetExternalMigrationStep<Page>()),
                new Migration<>("1.5.7", styleAssetMigrationStep),
                new Migration<>("1.5.10", uiBootstrapAssetMigrationStep),
                new Migration<>("1.7.4", pageTextWidgetInterpretHTMLMigrationStep),
                new Migration<>("1.7.25", pageUUIDMigrationStep),
                new Migration<>("1.8.29", styleAddModalContainerPropertiesMigrationStep),
                new Migration<>("1.9.24", pageTextWidgetLabelMigrationStep),
                new Migration<>("1.10.5", dynamicTabsContainerMigrationStep),
                new Migration<>("1.10.12", dataToVariableMigrationStep),
                new Migration<>("1.10.16", pageTableWidgetInterpretHTMLMigrationStep),
                new Migration<>("1.10.18", pageTableWidgetStylesMigrationStep));
    }

    @Bean
    public LiveRepositoryUpdate<Widget> widgetLiveRepositoryUpdate(
            WidgetFileBasedLoader widgetLoader,
            WidgetRepository widgetRepository) {
        return new LiveRepositoryUpdate<>(widgetRepository, widgetLoader, widgetMigrationSteps);
    }

    @Resource(name = "widgetMigrationStepsList")
    protected List<Migration<Widget>> widgetMigrationSteps;

    @Bean
    public List<Migration<Widget>> widgetMigrationStepsList() {
        return asList(
                new Migration<>("1.0.2", new AssetIdMigrationStep<Widget>()),
                new Migration<>("1.2.9", new AssetExternalMigrationStep<Widget>()),
                new Migration<>("1.10.12", new SplitWidgetResourcesMigrationStep())
        );
    }

    @Bean
    public PageService pageService(PageRepository pageRepository, PageMigrationApplyer pageMigrationApplyer) {
        return new PageService(pageRepository, pageMigrationApplyer);
    }

    @Bean
    public PageMigrationApplyer pageMigrationApplyer(WidgetService widgetService) {
        return new PageMigrationApplyer(pageMigrationSteps, widgetService);
    }

    @Bean
    public WidgetMigrationApplyer widgetMigrationApplyer() {
        return new WidgetMigrationApplyer(widgetMigrationSteps);
    }
}
