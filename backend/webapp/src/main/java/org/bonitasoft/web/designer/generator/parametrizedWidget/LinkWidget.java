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

import java.util.Objects;

@Widget
public class LinkWidget extends AbstractParametrizedWidget {

    private static final String LINK_WIDGET_ID = "pbLink";
    @WidgetProperty
    private String buttonStyle;
    @WidgetProperty
    private String targetUrl;
    @WidgetProperty
    private String text;
    @WidgetProperty
    private String alignment = Alignment.LEFT.getValue();


    public LinkWidget() {
        super(LINK_WIDGET_ID);
    }

    public String getButtonStyle() {
        return buttonStyle;
    }

    public void setButtonStyle(ButtonStyle buttonStyle) {
        if (Objects.equals(buttonStyle, ButtonStyle.DEFAULT)) {
            this.buttonStyle = ButtonStyle.LINK.getValue();
        } else {
            this.buttonStyle = buttonStyle.getValue();
        }
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment.getValue();
    }

}
