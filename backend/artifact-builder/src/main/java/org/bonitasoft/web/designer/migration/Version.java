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
package org.bonitasoft.web.designer.migration;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class Version {

    public static String INITIAL_MODEL_VERSION = "2.0";
    public static String INITIAL_UID_VERSION_USING_MODEL_VERSION = "1.12.0-SNAPSHOT";

    private DefaultArtifactVersion version;

    public Version(String version) {
        this.version = new DefaultArtifactVersion(version);
    }

    public static boolean isSupportingModelVersion(String version) {
        return version != null && new Version(version).isGreaterOrEqualThan(INITIAL_UID_VERSION_USING_MODEL_VERSION);
    }

    public boolean isGreaterThan(String version) {
        return this.version.compareTo(new DefaultArtifactVersion(version)) > 0;
    }

    public boolean isGreaterOrEqualThan(String version) {
        return this.version.compareTo(new DefaultArtifactVersion(version)) >= 0;
    }

    @Override
    public String toString() {
        return version.toString();
    }
}
