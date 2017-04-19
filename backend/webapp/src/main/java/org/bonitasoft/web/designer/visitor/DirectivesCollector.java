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

import static java.lang.String.format;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;

@Named
public class DirectivesCollector {

    private WidgetRepository widgetRepository;
    private WidgetIdVisitor widgetIdVisitor;

    @Inject
    public DirectivesCollector(WidgetRepository widgetRepository, WidgetIdVisitor widgetIdVisitor) {
        this.widgetRepository = widgetRepository;
        this.widgetIdVisitor = widgetIdVisitor;
    }

    public List<String> collect(Previewable previewable) {
        return widgetRepository.getByIds(widgetIdVisitor.visit(previewable)).stream()
                .filter(widget -> !isContainer(widget))
                .map(w -> format("widgets/%s/%s.js", w.getId(), w.getId()))
                .collect(Collectors.toList());
    }

    private boolean isContainer(Widget widget) {
        return "container".equals(widget.getType());
    }

}
