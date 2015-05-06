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
package org.bonitasoft.web.designer.model.page;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

@JsonTypeName("formContainer")
public class FormContainer extends Element {

    public static final String PARAM_ACTION = "action";
    public static final String PARAM_ACTION_DEFAULT_VALUE = "#";
    public static final String PARAM_METHOD = "method";
    public static final String PARAM_METHOD_DEFAULT_VALUE = "GET";

    private Container container = new Container();


    @JsonView({JsonViewPersistence.class})
    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    @JsonIgnore
    public String getAction(){
        return getValueParameter(PARAM_ACTION, PARAM_ACTION_DEFAULT_VALUE);
    }

    @JsonIgnore
    public String getMethod(){
        return getValueParameter(PARAM_METHOD, PARAM_METHOD_DEFAULT_VALUE);
    }

    @JsonIgnore
    private String getValueParameter(String key, String defaultValue){
        if(getPropertyValues().get(key)!=null){
            return (String) getPropertyValues().get(key).getValue();
        }
        return defaultValue;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FormContainer) {
            final FormContainer other = (FormContainer) obj;
            return new EqualsBuilder()
                    .append(getPropertyValues(), other.getPropertyValues())
                    .append(container, other.container)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(container)
                .append(getDimension())
                .append(getPropertyValues())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("container", container)
                .append("dimension", getDimension())
                .append("PropertyValues", getPropertyValues())
                .toString();
    }
}
