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
public class ButtonWidget extends LabelParametrizedWidget {

    private static final String BUTTON_WIDGET_ID = "pbButton";

    @WidgetProperty
    private String buttonStyle;

    @WidgetProperty
    private boolean disabled = false;

    @WidgetProperty
    private String action;

    @WidgetProperty
    private String dataToSend;

    @WidgetProperty
    private String collectionToModify;

    @WidgetProperty
    private String targetUrlOnSuccess;

    @WidgetProperty
    private String collectionPosition;

    @WidgetProperty
    private String valueToAdd;

    @WidgetProperty
    private String removeItem;

    @WidgetProperty
    private String alignment = Alignment.LEFT.getValue();

    public ButtonWidget() {
        super(BUTTON_WIDGET_ID);
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment.getValue();
    }

    public String getButtonStyle() {
        return buttonStyle;
    }

    public void setButtonStyle(ButtonStyle buttonStyle) {
        this.buttonStyle = buttonStyle.getValue();
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean isDisabled) {
        this.disabled = isDisabled;
    }

    public String getAction() {
        return action;
    }

    public void setAction(ButtonAction action) {
        this.action = action.getValue();
    }

    public String getDataToSend() {
        return dataToSend;
    }

    public void setDataToSend(String dataToSend) {
        this.dataToSend = dataToSend;
    }

    public String getCollectionToModify() {
        return collectionToModify;
    }

    public void setCollectionToModify(String collectionToModify) {
        this.collectionToModify = collectionToModify;
    }

    public String getTargetUrlOnSuccess() {
        return targetUrlOnSuccess;
    }

    public void setTargetUrlOnSuccess(String targetUrlOnSuccess) {
        this.targetUrlOnSuccess = targetUrlOnSuccess;
    }

    public void setCollectionPosition(String collectionPosition) {
        this.collectionPosition = collectionPosition;
    }

    public String getCollectionPosition() {
        return collectionPosition;
    }

    public void setRemoveItem(String removeItem) {
        this.removeItem = removeItem;
    }

    public String getRemoveItem() {
        return removeItem;
    }

    public String getValueToAdd() {
        return valueToAdd;
    }

    public void setValueToAdd(String valueToAdd) {
        this.valueToAdd = valueToAdd;
    }
}
