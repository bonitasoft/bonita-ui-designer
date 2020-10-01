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

import org.bonitasoft.web.designer.migration.page.AutocompleteWidgetReturnedKeyMigrationStep;
import org.bonitasoft.web.designer.migration.page.BondMigrationStep;
import org.bonitasoft.web.designer.migration.page.BusinessVariableMigrationStep;
import org.bonitasoft.web.designer.migration.page.DataToVariableMigrationStep;
import org.bonitasoft.web.designer.migration.page.DynamicTabsContainerMigrationStep;
import org.bonitasoft.web.designer.migration.page.PageUUIDMigrationStep;
import org.bonitasoft.web.designer.migration.page.TableWidgetInterpretHTMLMigrationStep;
import org.bonitasoft.web.designer.migration.page.TableWidgetStylesMigrationStep;
import org.bonitasoft.web.designer.migration.page.TextWidgetInterpretHTMLMigrationStep;
import org.bonitasoft.web.designer.migration.page.TextWidgetLabelMigrationStep;
import org.bonitasoft.web.designer.migration.page.UIBootstrapAssetMigrationStep;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.JsonFileBasedLoader;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.service.FragmentMigrationApplyer;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageMigrationApplyer;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetMigrationApplyer;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.bonitasoft.web.designer.visitor.VisitorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MigrationConfig {

    public static String INITIAL_MODEL_VERSION = "2.0";
    public static String INITIAL_UID_VERSION_USING_MODEL_VERSION = "1.12.0-snapshot";

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
    public AutocompleteWidgetReturnedKeyMigrationStep<Page> pageAutocompleteWidgetReturnedKeyMigrationStep(ComponentVisitor componentVisitor) {
        return new AutocompleteWidgetReturnedKeyMigrationStep(componentVisitor);
    }

    @Bean
    public AddModelVersionMigrationStep<Page> pageAddModelVersionMigrationStep(ComponentVisitor componentVisitor) {
        return new AddModelVersionMigrationStep();
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

    @Bean
    public BusinessVariableMigrationStep<Page> pageBusinessVariableMigrationStep() {
        return new BusinessVariableMigrationStep<>();
    }

    @Bean
    public BondMigrationStep<Fragment> fragmentBondMigrationStep(ComponentVisitor componentVisitor,
                                                                 WidgetRepository widgetRepository,
                                                                 VisitorFactory visitorFactory) {
        return new BondMigrationStep(componentVisitor, widgetRepository, visitorFactory);
    }

    @Bean
    public TextWidgetInterpretHTMLMigrationStep<Fragment> fragmentTextWidgetInterpretHTMLMigrationStep(ComponentVisitor componentVisitor) {
        return new TextWidgetInterpretHTMLMigrationStep(componentVisitor);
    }

    @Bean
    public TableWidgetInterpretHTMLMigrationStep<Fragment> fragmentDataTableWidgetInterpretHTMLMigrationStep(ComponentVisitor componentVisitor) {
        return new TableWidgetInterpretHTMLMigrationStep<Fragment>(componentVisitor);
    }

    @Bean
    public TableWidgetStylesMigrationStep<Fragment> fragmentTableWidgetStylesMigrationStep(ComponentVisitor componentVisitor) {
        return new TableWidgetStylesMigrationStep<Fragment>(componentVisitor);
    }

    @Bean
    public TextWidgetLabelMigrationStep<Fragment> fragmentTextWidgetLabelMigrationStep(ComponentVisitor componentVisitor) {
        return new TextWidgetLabelMigrationStep(componentVisitor);
    }

    @Bean
    public AutocompleteWidgetReturnedKeyMigrationStep<Fragment> fragmentAutocompleteWidgetReturnedKeyMigrationStep(ComponentVisitor componentVisitor) {
        return new AutocompleteWidgetReturnedKeyMigrationStep(componentVisitor);
    }

    @Bean
    public AddModelVersionMigrationStep<Fragment> fragmentAddModelVersionMigrationStep(ComponentVisitor componentVisitor) {
        return new AddModelVersionMigrationStep<Fragment>();
    }

    @Bean
    public DataExposedMigrationStep<Fragment> fragmentDataExposedMigrationStep() {
        return new DataExposedMigrationStep();
    }

    @Bean
    public LiveRepositoryUpdate<Fragment> fragmentLiveRepositoryUpdate(
            JsonFileBasedLoader<Fragment> fragmentFileBasedLoader,
            FragmentRepository fragmentRepository) {
        return new LiveRepositoryUpdate(fragmentRepository, fragmentFileBasedLoader, fragmentMigrationSteps);
    }

    @Resource(name = "fragmentMigrationStepsList")
    private List<Migration<Fragment>> fragmentMigrationSteps;

    @Bean
    public List<Migration<Fragment>> fragmentMigrationStepsList(BondMigrationStep<Fragment> fragmentBondMigrationStep,
                                                                TextWidgetInterpretHTMLMigrationStep<Fragment> fragmentTextWidgetInterpretHTMLMigrationStep,
                                                                TextWidgetLabelMigrationStep<Fragment> fragmentTextWidgetLabelMigrationStep,
                                                                DataToVariableMigrationStep<Fragment> fragmentDataToVariableMigrationStep,
                                                                TableWidgetInterpretHTMLMigrationStep<Fragment> fragmentTablesWidgetInterpretHTMLMigrationStep,
                                                                TableWidgetStylesMigrationStep<Fragment> fragmentTableWidgetStylesMigrationStep,
                                                                AutocompleteWidgetReturnedKeyMigrationStep<Fragment> fragmentAutocompleteWidgetReturnedKeyMigrationStep,
                                                                DataExposedMigrationStep<Fragment> dataExposedMigrationStep,
                                                                AddModelVersionMigrationStep<Fragment> fragmentAddModelVersionMigrationStep) {
        return asList(
                new Migration<>("1.0.3", fragmentBondMigrationStep),
                new Migration<>("1.7.25", fragmentTextWidgetInterpretHTMLMigrationStep),
                new Migration<>("1.9.24", fragmentTextWidgetLabelMigrationStep),
                new Migration<>("1.10.12", fragmentDataToVariableMigrationStep),
                new Migration<>("1.10.16", fragmentTablesWidgetInterpretHTMLMigrationStep),
                new Migration<>("1.10.18", fragmentTableWidgetStylesMigrationStep),
                new Migration<>("1.11.46", dataExposedMigrationStep),
                new Migration<>(INITIAL_MODEL_VERSION, fragmentAddModelVersionMigrationStep, fragmentAutocompleteWidgetReturnedKeyMigrationStep));
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
            TableWidgetStylesMigrationStep<Page> pageTableWidgetStylesMigrationStep,
            AutocompleteWidgetReturnedKeyMigrationStep<Page> pageAutocompleteWidgetReturnedKeyMigrationStep,
            BusinessVariableMigrationStep<Page> pageBusinessVariableMigrationStep,
            AddModelVersionMigrationStep<Page> pageAddModelVersionMigrationStep,
            StyleUpdateInputRequiredLabelMigrationStep styleUpdateInputRequiredLabelMigrationStep) {
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
                new Migration<>("1.10.18", pageTableWidgetStylesMigrationStep),
                new Migration<>("1.11.40", pageBusinessVariableMigrationStep),
                new Migration<>("1.11.46", styleUpdateInputRequiredLabelMigrationStep),
                new Migration<>(INITIAL_MODEL_VERSION, pageAddModelVersionMigrationStep, pageAutocompleteWidgetReturnedKeyMigrationStep));
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
                new Migration<>("1.10.12", new SplitWidgetResourcesMigrationStep()),
                new Migration<>(INITIAL_MODEL_VERSION, new AddModelVersionMigrationStep<Widget>()));
    }

    @Bean
    public PageService pageService(PageRepository pageRepository, PageMigrationApplyer pageMigrationApplyer) {
        return new PageService(pageRepository, pageMigrationApplyer);
    }

    @Bean
    @Primary
    public PageMigrationApplyer pageMigrationApplyer(WidgetService widgetService, FragmentService fragmentService) {
        return new PageMigrationApplyer(pageMigrationSteps, widgetService, fragmentService);
    }

    @Bean
    public WidgetMigrationApplyer widgetMigrationApplyer() {
        return new WidgetMigrationApplyer(widgetMigrationSteps);
    }

    @Bean
    public FragmentMigrationApplyer fragmentMigrationApplyer(WidgetService widgetService) {
        return new FragmentMigrationApplyer(fragmentMigrationSteps, widgetService);
    }

    public static boolean isSupportingModelVersion(String version) {
        return version != null && new Version(version).isGreaterOrEqualThan(MigrationConfig.INITIAL_UID_VERSION_USING_MODEL_VERSION);
    }
}
