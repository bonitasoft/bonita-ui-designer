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

import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.generator.mapping.DimensionFactory;
import org.bonitasoft.web.designer.generator.parametrizedWidget.WidgetContainer;
import org.bonitasoft.web.designer.model.page.Container;

@Named
public class DataManagementGenerator {

    private DimensionFactory dimensionFactory;

    @Inject
    public DataManagementGenerator(DimensionFactory dimensionFactory) {
        this.dimensionFactory = dimensionFactory;
    }

    /**
     * Parse NodeBusinessObjectInput to map each input as widget and push them into businessObjectContainer
     *
     * @param nodeBusinessObjectInput
     */
    public BusinessObjectContainer generate(BusinessObject nodeBusinessObjectInput) {
        BusinessObjectContainer boc = createBusinessObjectContainer(nodeBusinessObjectInput);

        BusinessDataToWidgetMapper businessDataToWidgetMapper = new BusinessDataToWidgetMapper(dimensionFactory, boc);
        BusinessObjectVisitorImpl businessObjectVisitor = new BusinessObjectVisitorImpl(boc.getContainer(),
                businessDataToWidgetMapper);

        nodeBusinessObjectInput.accept(businessObjectVisitor);

        return boc;
    }

    private BusinessObjectContainer createBusinessObjectContainer(BusinessObject nodeBusinessObjectInput) {
        WidgetContainer wc = new WidgetContainer();
        wc.setDimension(12);
        Container container = wc.toContainer(dimensionFactory);
        BusinessObjectContainer boc = new BusinessObjectContainer(container);
        boc.getContainer().setDescription(WidgetDescription.ROOT_AUTOGENERATE_CONTAINER.displayValue(nodeBusinessObjectInput.getInput().get(0).getName()));
        return boc;
    }
}
