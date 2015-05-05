/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.builder;

import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.PropertyValue;

public class FormContainerBuilder extends ElementBuilder<FormContainer> {

    private FormContainer formContainer;
    private String reference = "formcontainer-reference";

    private FormContainerBuilder() {
        formContainer = new FormContainer();
        formContainer.setReference(reference);
    }

    public static FormContainerBuilder aFormContainer() {
        return new FormContainerBuilder();
    }

    public FormContainerBuilder with(Container container) {
        formContainer.setContainer(container);
        return this;
    }

    public FormContainerBuilder name(String name) {
        setParameter("name", name);
        return this;
    }


    public FormContainerBuilder action(String action) {
        setParameter("action", action);
        return this;
    }

    public FormContainerBuilder method(String method) {
        setParameter("method", method);
        return this;
    }

    public FormContainerBuilder addParam(String key, String type, Object value) {
        PropertyValue parameterValue = new PropertyValue();
        parameterValue.setValue(value);
        parameterValue.setType(type);
        formContainer.getPropertyValues().put(key, parameterValue);
        return this;
    }

    @Override
    public FormContainer build() {
        return formContainer;
    }

    @Override
    public FormContainer getElement() {
        return formContainer;
    }

    private void setParameter(String key, String name) {
        PropertyValue value = formContainer.getPropertyValues().get(key);
        if(value==null){
            value = new PropertyValue();
            value.setType("string");
            value.setValue(name);
            formContainer.getPropertyValues().put(key, value);
        }
        else{
            value.setValue(name);
        }
    }

}
