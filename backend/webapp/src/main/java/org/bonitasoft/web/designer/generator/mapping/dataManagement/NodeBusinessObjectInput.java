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
package org.bonitasoft.web.designer.generator.mapping.dataManagement;

import org.bonitasoft.web.designer.model.contract.NodeContractInput;

public class NodeBusinessObjectInput extends NodeContractInput {

    static final String BUSINESS_OBJECT_SELECTED = "_selected";

    /**
     * UID variable name
     */
    private String pageDataName;
    private String businessObjectAttributeName;

    public NodeBusinessObjectInput(String qualifiedName) {
        super(qualifiedName);
    }

    public NodeBusinessObjectInput(String qualifiedName, String pageDataName) {
        super(qualifiedName);
        this.pageDataName = pageDataName;
    }

    public NodeBusinessObjectInput(String qualifiedName, String pageDataName, String businessObjectAttributeName) {
        super(qualifiedName);
        this.pageDataName = pageDataName;
        this.businessObjectAttributeName = businessObjectAttributeName;
    }

    public String getPageDataName() {
        return pageDataName;
    }

    public String getPageDataNameSelected() {
        return new StringBuilder(pageDataName).append(BUSINESS_OBJECT_SELECTED).toString();
    }

    public void setPageDataName(String pageDataName) {
        this.pageDataName = pageDataName;
    }

    public String getBusinessObjectAttributeName() {
        return businessObjectAttributeName;
    }

    public void setBusinessObjectAttributeName(String businessObjectAttributeName) {
        this.businessObjectAttributeName = businessObjectAttributeName;
    }

    public String formatName() {
        String name = this.getName().substring(this.getName().lastIndexOf(".") + 1);
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public boolean isRootOrMultipleInput() {
        return this.isMultiple() || this.getParent() == null;
    }

    public String getDataName(NodeBusinessObjectInput nodeBusinessObjectInput) {
        NodeBusinessObjectInput node = (NodeBusinessObjectInput) nodeBusinessObjectInput.getParent();
        return node.isRootOrMultipleInput() ? node.getPageDataNameSelected() : node.getPageDataName();
    }
}
