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
public class TextWidget extends LabelParametrizedWidget {

    private static final String LABEL_WIDGET_ID = "pbText";

    @WidgetProperty
    private String text;
    @WidgetProperty
    private boolean allowHtml = true;
    @WidgetProperty
    private String alignment = Alignment.LEFT.getValue();

    public TextWidget() {
        super(LABEL_WIDGET_ID);
        setLabelHidden(true);
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment.getValue();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getAllowHtml() {
        return allowHtml;
    }

    public void setAllowHtml(boolean allowHtml) {
        this.allowHtml = allowHtml;
    }
}
