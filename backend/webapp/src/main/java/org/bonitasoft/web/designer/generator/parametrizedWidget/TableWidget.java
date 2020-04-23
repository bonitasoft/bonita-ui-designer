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

import static org.bonitasoft.web.designer.generator.parametrizedWidget.ParameterType.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.CaseFormat;
import org.bonitasoft.web.designer.model.page.PropertyValue;

@Widget
public class TableWidget extends AbstractParametrizedWidget {

    private static final String LABEL_WIDGET_ID = "pbTable";
    @WidgetProperty
    private List<String> headers;
    @WidgetProperty
    private List<String> columnsKey;
    @WidgetProperty
    private String selectedRow;
    @WidgetProperty
    private String content;
    @WidgetProperty
    private boolean allowHtml = true;
    @WidgetProperty
    private boolean zebraStriping = true;
    @WidgetProperty
    private boolean condensed = false;
    @WidgetProperty
    private boolean bordered = false;


    public TableWidget() {
        super(LABEL_WIDGET_ID);
    }

    public List<String> getHeaders() {
        return headers;
    }


    public void setHeaders(List<String> headers) {
        this.headers = headers.stream().map(header -> displayHeader(header)).collect(Collectors.toList());
    }

    public List<String> getColumnsKey() {
        return columnsKey;
    }

    public void setColumnsKey(List<String> columnKeys) {
        this.columnsKey = columnKeys;
    }

    public String getSelectedRow() {
        return selectedRow;
    }

    public void setSelectedRow(String selectedRow) {
        this.selectedRow = selectedRow;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isZebraStriping() {
        return zebraStriping;
    }

    public void setZebraStriping(boolean zebraStriping) {
        this.zebraStriping = zebraStriping;
    }

    public boolean isCondensed() {
        return condensed;
    }

    public void setCondensed(boolean condensed) {
        this.condensed = condensed;
    }

    public boolean isBordered() {
        return bordered;
    }

    public void setBordered(boolean bordered) {
        this.bordered = bordered;
    }

    public boolean getAllowHtml() {
        return allowHtml;
    }

    public void setAllowHtml(boolean allowHtml) {
        this.allowHtml = allowHtml;
    }

    @Override
    protected PropertyValue createPropertyValue(Map.Entry<String, Object> entry) {
        if (Objects.equals(entry.getKey(), CONTENT_PARAMETER)) {
            return createPropertyValue(EXPRESSION, this.content);
        }
        if (Objects.equals(entry.getKey(), SELECTED_ROW_PARAMETER)) {
            return createPropertyValue(ParameterType.VARIABLE, this.selectedRow);
        }
        return super.createPropertyValue(entry);
    }

    private String displayHeader(String header) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, header)
                .replaceAll("((?<=[a-z])(?=[A-Z]))|((?<=[A-Z])(?=[A-Z][a-z]))", " ");
    }
}
