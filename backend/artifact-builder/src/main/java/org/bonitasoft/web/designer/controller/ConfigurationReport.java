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

package org.bonitasoft.web.designer.controller;

public class ConfigurationReport {

    private String uidVersion;
    private String modelVersion;
    private String bdrUrl;
    private boolean experimentalMode;

    public ConfigurationReport(String uidVersion, String modelVersion, String bdrUrl, boolean isExperimental) {
        this.uidVersion = uidVersion;
        this.modelVersion = modelVersion;
        this.bdrUrl = bdrUrl;
        this.experimentalMode = isExperimental;
    }

    public String getUidVersion() {
        return uidVersion;
    }

    public void setUidVersion(String uidVersion) {
        this.uidVersion = uidVersion;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getBdrUrl() {
        return bdrUrl;
    }

    public void setBdrUrl(String bdrUrl) {
        this.bdrUrl = bdrUrl;
    }

    public boolean isExperimentalMode() {
        return experimentalMode;
    }

    public void setExperimentalMode(boolean experimentalMode) {
        this.experimentalMode = experimentalMode;
    }

    @Override
    public String toString() {
        return "{" +
                "\"uidVersion\":" + uidVersion +
                ",\"modelVersion\":" + modelVersion +
                ",\"bdrUrl\":" + bdrUrl +
                ",\"experimentalMode\":" + experimentalMode +
                '}';
    }
}
