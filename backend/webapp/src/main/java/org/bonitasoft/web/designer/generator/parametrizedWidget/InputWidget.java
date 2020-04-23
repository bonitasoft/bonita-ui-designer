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

@Widget
public class InputWidget extends LabelParametrizedWidget implements Labeled, Valuable, Requirable {

    static final String INPUT_WIDGET_ID = "pbInput";

    @WidgetProperty
    private boolean readOnly = false;

    @WidgetProperty
    private String type;

    @WidgetProperty
    private String placeholder;

    @WidgetProperty
    private String value;

    @WidgetProperty
    private boolean required = true;

    @WidgetProperty
    private String labelPosition = LabelPosition.LEFT.getValue();

    @WidgetProperty
    private int labelWidth = 1;

    @WidgetProperty
    private boolean labelHidden = false;

    public InputWidget(String widgetId) {
        super(widgetId);
    }

    public InputWidget() {
        super(INPUT_WIDGET_ID);
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

    public void setLabelWidth(int labelWidth) {
        this.labelWidth = labelWidth;
    }

    public String getType() {
        return type;
    }

    public void setType(InputType type) {
        this.type = type.getValue();
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

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }


    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }


    public boolean isLabelHidden() {
        return labelHidden;
    }

    public void setLabelHidden(boolean labelHidden) {
        this.labelHidden = labelHidden;
    }
}
