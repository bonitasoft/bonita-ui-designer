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
package org.bonitasoft.web.designer.experimental.parametrizedWidget;

public class ButtonWidget extends AbstractParametrizedWidget {

    private static final String BUTTON_WIDGET_ID = "pbButton";
    private String buttonStyle;
    private boolean disabled = false;
    private String action;
    private String dataToSend;
    private String collectionToModify;
    private String targetUrlOnSuccess;
    private String collectionPosition;

    public ButtonWidget() {
        super(BUTTON_WIDGET_ID);
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
}
