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
package org.bonitasoft.web.designer.model.page;

import java.util.List;
import java.util.Map;

import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;

/**
 * Interface of model elements that can be previewed
 *
 * @author JB Nizet
 */
public interface Previewable {

    /**
     * Gets the rows of this previewable
     */
    List<List<Element>> getRows();

    /**
     * Gets the data used when previewing this previewable
     */
    Map<String, Data> getData();

    /**
     * Gets the name of a previewable object
     */
    String getName();

    void addData(String name, Data value);

    void removeData(String dataName) throws NotFoundException;

}
