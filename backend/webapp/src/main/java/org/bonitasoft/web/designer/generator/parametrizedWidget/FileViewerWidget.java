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
package org.bonitasoft.web.designer.generator.parametrizedWidget;


import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.generator.mapping.DimensionFactory;
import org.bonitasoft.web.designer.model.ParameterType;

import java.util.Map;

@Widget
public class FileViewerWidget extends AbstractParametrizedWidget {

    static final String FILE_VIEWER_WIDGET_ID = "pbFileViewer";

    private static final String DOCUMENT_TYPE = "Process document";

    @WidgetProperty
    private String document;

    @WidgetProperty
    private boolean showPreview = false;

    public FileViewerWidget() {
        super(FILE_VIEWER_WIDGET_ID);
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public boolean isShowPreview() {
        return showPreview;
    }

    public void setShowPreview(boolean showPreview) {
        this.showPreview = showPreview;
    }

    @Override
    public Component toComponent(DimensionFactory dimensionFactory) {
        Component component = super.toComponent(dimensionFactory);
        Map<String, PropertyValue> values = component.getPropertyValues();
        values.put("type", createPropertyValue(ParameterType.CONSTANT, DOCUMENT_TYPE));
        values.put("showPreview", createPropertyValue(ParameterType.CONSTANT, isShowPreview()));
        values.put("document", createPropertyValue(ParameterType.VARIABLE, getDocument()));
        values.put("url", createPropertyValue(ParameterType.CONSTANT, null));
        return component;
    }


}
