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
package org.bonitasoft.web.designer.model.contract;

public class DataReference {

    private String name;
    private String type;
    private RelationType relationType;
    private LoadingType loadingType;

    public enum RelationType {
        COMPOSITION, AGGREGATION
    };

    public enum LoadingType {
        EAGER, LAZY
    };

    public DataReference(String name,String type, RelationType relationType, LoadingType loadingType) {
        this.name = name;
        this.type = type;
        this.relationType = relationType;
        this.loadingType = loadingType;
    }

    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }
    
    
    public String getType() {
        return type;
    }
    
    
    public void setType(String type) {
        this.type = type;
    }

    
    public RelationType getRelationType() {
        return relationType;
    }

    
    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    
    public LoadingType getLoadingType() {
        return loadingType;
    }

    
    public void setLoadingType(LoadingType loadingType) {
        this.loadingType = loadingType;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((loadingType == null) ? 0 : loadingType.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((relationType == null) ? 0 : relationType.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataReference other = (DataReference) obj;
        if (loadingType != other.loadingType)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (relationType != other.relationType)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
    
    

}
