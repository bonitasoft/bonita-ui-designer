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
package org.bonitasoft.web.designer.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.Tab;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.repository.WidgetRepository;

/**
 * An element visitor which traverses the tree of elements recursively to collect html parts of a page
 */
public class HtmlBuilderVisitor implements ElementVisitor<String> {

    private WidgetRepository widgetRepository;
    private WidgetIdVisitor widgetIdVisitor;
    private PropertyValuesVisitor propertyValuesVisitor;
    private DataModelVisitor dataModelVisitor;
    private RequiredModulesVisitor requiredModulesVisitor;

    class TabTemplate {
        private final String title;
        private final String content;

        public TabTemplate(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }
    }

    public HtmlBuilderVisitor(WidgetRepository widgetRepository,
                              WidgetIdVisitor widgetIdVisitor,
                              PropertyValuesVisitor propertyValuesVisitor,
                              DataModelVisitor dataModelVisitor,
                              RequiredModulesVisitor requiredModulesVisitor) {
        this.widgetRepository = widgetRepository;
        this.widgetIdVisitor = widgetIdVisitor;
        this.propertyValuesVisitor = propertyValuesVisitor;
        this.dataModelVisitor = dataModelVisitor;
        this.requiredModulesVisitor = requiredModulesVisitor;
    }

    @Override
    public String visit(Container container) {

        return new TemplateEngine("container.hbs.html")
                .with("rowsHtml", buildRowsHtml(container.getRows()))
                .build(container);
    }

    @Override
    public String visit(FormContainer formContainer) {
        return new TemplateEngine("formContainer.hbs.html")
                .with("content", formContainer.getContainer().accept(this))
                .build(formContainer);
    }

    @Override
    public String visit(TabsContainer tabsContainer) {

        List<TabTemplate> tabTemplates = new ArrayList<>();
        for (Tab tab : tabsContainer.getTabs()) {
            tabTemplates.add(new TabTemplate(tab.getTitle(), tab.getContainer().accept(this)));
        }

        return new TemplateEngine("tabsContainer.hbs.html")
                .with("tabTemplates", tabTemplates)
                .build(tabsContainer);
    }


    @Override
    public String visit(Component component) {

        return new TemplateEngine("component.hbs.html")
                .with("template", "<" + Widget.spinalCase(component.getWidgetId()) + "></" + Widget.spinalCase(component.getWidgetId()) + ">")
                .build(component);
    }

    @Override
    public String visit(Previewable previewable) {
        throw new RuntimeException("Can't build previewable html by visiting it. Need to call HtmlBuilderVisitor#build.");
    }

    /**
     * Build a previewable HTML, based on the given list of widgets
     *
     * TODO: once resourceContext remove we can merge this method with HtmlBuilderVisitor#visit(Previewable)
     *
     * @param previewable     to build
     * @param resourceContext the URL context can change on export or preview...
     */
    public <P extends Previewable & Identifiable> String build(P previewable, String resourceContext) {
        TemplateEngine template = new TemplateEngine("page.hbs.html")
                .with("resourceContext", resourceContext == null ? "" : resourceContext)
                .with("widgets", widgetRepository.getByIds(widgetIdVisitor.visit(previewable)))
                .with("rowsHtml", buildRowsHtml(previewable.getRows()))
                .with("dataModelFactory", dataModelVisitor.generateFactory(previewable))
                .with("propertyValuesFactory", propertyValuesVisitor.generateFactory(previewable));

        Set<String> modules = requiredModulesVisitor.visit(previewable);
        if (!modules.isEmpty()) {
            template = template.with("modules", modules);
        }
        return template.build(previewable);
    }

    private List<String> buildRowsHtml(List<List<Element>> rows) {

        List<String> rowsHtml = new ArrayList<>();
        for (List<Element> row : rows) {
            StringBuilder rowHtml = new StringBuilder();
            for (Element element : row) {
                rowHtml.append(element.accept(this));
            }
            rowsHtml.add(rowHtml.toString());
        }
        return rowsHtml;
    }


}
