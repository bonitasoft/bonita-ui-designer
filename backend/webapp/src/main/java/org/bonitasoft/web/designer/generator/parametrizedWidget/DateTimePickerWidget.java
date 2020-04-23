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
public class DateTimePickerWidget extends InputWidget {

    private static final String DATE_TIME_PICKER_WIDGET_ID = "pbDateTimePicker";

    @WidgetProperty
    private String dateFormat;

    @WidgetProperty
    private String timeFormat;

    @WidgetProperty
    private boolean showNow = true;

    @WidgetProperty
    private boolean showToday = true;

    @WidgetProperty
    private String nowLabel;

    @WidgetProperty
    private String todayLabel;

    @WidgetProperty
    private boolean withTimeZone = false;

    @WidgetProperty
    private boolean inlineInput = true;

    @WidgetProperty
    private String timePlaceholder;

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

    public String getTimePlaceholder() {return timePlaceholder; }

    public void setTimePlaceholder(String timePlaceholder) { this.timePlaceholder = timePlaceholder; }

    public String getNowLabel() {
        return nowLabel;
    }

    public void setNowLabel(String nowLabel) {
        this.nowLabel = nowLabel;
    }

    public String getTodayLabel() {
        return todayLabel;
    }

    public void setTodayLabel(String todayLabel) {
        this.todayLabel = todayLabel;
    }

    public boolean getInlineInput() {
        return inlineInput;
    }

    public void setInlineInput(boolean inlineInput) {
        this.inlineInput = inlineInput;
    }
}
