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
public class DatePickerWidget extends InputWidget {

    private static final String DATE_PICKER_WIDGET_ID = "pbDatePicker";
    @WidgetProperty
    private String dateFormat;
    @WidgetProperty
    private boolean showToday = true;
    @WidgetProperty
    private String todayLabel;

    public DatePickerWidget() {
        super(DATE_PICKER_WIDGET_ID);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public boolean isShowToday() {
        return showToday;
    }

    public void setShowToday(boolean showToday) {
        this.showToday = showToday;
    }

    public String getTodayLabel() {
        return todayLabel;
    }

    public void setTodayLabel(String todayLabel) {
        this.todayLabel = todayLabel;
    }


}
