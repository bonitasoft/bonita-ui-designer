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

public class DateTimePickerWidget extends InputWidget {

    private static final String DATE_TIME_PICKER_WIDGET_ID = "pbDateTimePicker";
    private String dateFormat;
    private String timeFormat;
    private boolean showNow = true;
    private boolean showToday = true;

    private boolean withTimeZone = false;
    private boolean inlineInput = true;

    public DateTimePickerWidget() {
        super(DATE_TIME_PICKER_WIDGET_ID);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public boolean isWithTimeZone() {
        return withTimeZone;
    }

    public void setWithTimeZone(boolean withTimeZone) {
        this.withTimeZone = withTimeZone;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public boolean isShowNow() {
        return showNow;
    }

    public void setShowNow(boolean showNow) {
        this.showNow = showNow;
    }

    public boolean isShowToday() {
        return showToday;
    }

    public void setShowToday(boolean showToday) {
        this.showToday = showToday;
    }

    public boolean getInlineInput() {
        return inlineInput;
    }
}
