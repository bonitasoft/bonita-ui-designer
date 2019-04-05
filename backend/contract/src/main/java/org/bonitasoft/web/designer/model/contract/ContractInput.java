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

public interface ContractInput extends ContractInputContainer {

    String getName();

    String getType();

    String getDescription();

    EditMode getMode();

    boolean isMandatory();

    boolean isMultiple();

    void accept(ContractInputVisitor visitor);

    ContractInput getParent();

    void setParent(ContractInput parentInput);

    void setType(String classname);

    void setDescription(String description);

    void setMultiple(boolean multiple);

    void setMandatory(boolean mandatory);

    void setMode(EditMode mode);

    boolean isReadOnly();

    void setReadonly(boolean readOnly);

}
