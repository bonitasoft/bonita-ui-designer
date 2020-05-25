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

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.bonitasoft.web.designer.model.page.PropertyValue;

@Widget
public class FileUploadWidget extends LabelParametrizedWidget implements Valuable, Labeled, Requirable {

    static final String FILE_UPLOAD_WIDGET_ID = "pbUpload";
    private Optional<String> requiredExpression = Optional.empty();


    @WidgetProperty
    private String labelPosition = LabelPosition.LEFT.getValue();

    @WidgetProperty
    private int labelWidth = 1;

    @WidgetProperty
    private String placeholder;

    @WidgetProperty
    private String value;

    @WidgetProperty
    private String url = "../API/formFileUpload";

    @WidgetProperty
    private boolean required = true;


    public FileUploadWidget() {
        super(FILE_UPLOAD_WIDGET_ID);
    }

    public String getLabelPosition() {
        return labelPosition;
    }

    public void setLabelPosition(LabelPosition labelPosition) {
        this.labelPosition = labelPosition.getValue();
    }

    public int getLabelWidth() {
        return labelWidth;
    }

    @Override
    public void setLabelWidth(int labelWidth) {
        this.labelWidth = labelWidth;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Dynamic read only state for the generated widget
     */
    public void setRequiredExpression(String requiredExpression) {
        this.requiredExpression = Optional.ofNullable(requiredExpression);
    }

    /**
     * For test purpose
     */
    protected Optional<String> getRequiredExpression() {
        return requiredExpression;
    }

    @Override
    protected PropertyValue createPropertyValue(Entry<String, Object> entry) {
        if (Objects.equals(entry.getKey(), REQUIRED_PARAMETER) && requiredExpression.isPresent()) {
            return createPropertyValue(ParameterType.EXPRESSION, requiredExpression.get());
        }
        return super.createPropertyValue(entry);
    }
}
