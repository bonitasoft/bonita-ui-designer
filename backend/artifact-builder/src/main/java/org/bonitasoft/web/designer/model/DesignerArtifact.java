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
package org.bonitasoft.web.designer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.page.WebResource;

import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;

@JsonIgnoreProperties(value = {"designerVersion"}, allowSetters = true, ignoreUnknown = true)
public abstract class DesignerArtifact implements Identifiable {

    private String modelVersion;
    @JsonView({JsonViewPersistence.class})
    private String designerVersion;
    private String previousDesignerVersion; // used to be able to read 'old' artifacts
    private String previousArtifactVersion;
    private boolean favorite = false;

    // This field is only in memory, we don't need to store it
    private MigrationStatusReport status;

    private Set<WebResource> webResources = new HashSet<>();

    @JsonView({JsonViewPersistence.class})
    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String version) {
        this.modelVersion = version;
    }

    public void setModelVersionIfEmpty(String version) {
        if (isBlank(modelVersion) || modelVersion.split("_").length > 1) {
            setModelVersion(version);
        }
    }

    public String getDesignerVersion() {
        return designerVersion;
    }

    public void setDesignerVersion(String version) {
        this.designerVersion = version;
    }

    public void setDesignerVersionIfEmpty(String version) {
        if (isBlank(designerVersion) || designerVersion.split("_").length > 1) {
            setDesignerVersion(version);
        }
    }

    @JsonView({JsonViewPersistence.class})
    public String getPreviousArtifactVersion() {
        return previousArtifactVersion;
    }

    public void setPreviousArtifactVersion(String version) {
        this.previousArtifactVersion = version;
    }

    @JsonView({JsonViewPersistence.class})
    public String getPreviousDesignerVersion() {
        return previousDesignerVersion;
    }

    public void setPreviousDesignerVersion(String version) {
        this.previousArtifactVersion = version;
    }

    @JsonIgnore
    public String getArtifactVersion() {
        // Use model version if it is present
        if (getModelVersion() != null) {
            return getModelVersion();
        } else {
            return getDesignerVersion();
        }
    }

    @Override
    @JsonView({JsonViewMetadata.class, JsonViewLight.class})
    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @JsonView({JsonViewPersistence.class, JsonViewLight.class})
    public abstract String getType();

    @JsonView({JsonViewLight.class})
    public MigrationStatusReport getStatus() {
        return this.status;
    }

    @JsonIgnore
    public void setStatus(MigrationStatusReport status) {
        this.status = status;
    }

    @JsonView({JsonViewPersistence.class})
    public Set<WebResource> getWebResources() {
        return this.webResources;
    }

    public void setWebResources(Set<WebResource> webResources) {
        this.webResources = webResources;
    }
}
