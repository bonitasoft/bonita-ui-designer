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
package org.bonitasoft.web.designer.model.fragment;

import static org.apache.commons.lang3.text.WordUtils.capitalize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.designer.model.page.FragmentElement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Container;


public class Fragment extends AbstractPage {

    private String type = "fragment";
    private Map<String, List<Identifiable>> usedBy; // list of element that use this fragment

    @JsonView({JsonViewLight.class})
    public Map<String, List<Identifiable>> getUsedBy() {
        return usedBy;
    }

    public void addUsedBy(String componentName, List<Identifiable> components) {
        if (components != null && !components.isEmpty()) {
            if (usedBy == null)
                usedBy = new HashMap<>();
            usedBy.put(componentName, components);
        }
    }

    /*
     * As fragment extend page we need to provide a type. This type is actually not used.
     */
    @Override
    public String getType() {
        return type;
    }

    @JsonIgnore
    public boolean isUsed() {
        return getUsedBy() != null && !getUsedBy().isEmpty();
    }

    @JsonIgnore
    public String getDirectiveName() {
        return "pbFragment" + capitalize(getName());
    }

    @JsonIgnore
    public Map<String, Variable> getExposedVariables() {
        return Maps.filterValues(getVariables(), new Predicate<Variable>() {

            @Override
            public boolean apply(Variable variable) {
                return variable.isExposed();
            }
        });
    }

    public Container toContainer(FragmentElement fragmentElement) {
        Container container = new Container();
        container.setRows(getRows());
        container.setReference(fragmentElement.getReference());
        container.setPropertyValues(fragmentElement.getPropertyValues());
        return container;
    }
}
