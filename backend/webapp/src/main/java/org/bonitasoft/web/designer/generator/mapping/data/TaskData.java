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
package org.bonitasoft.web.designer.generator.mapping.data;

import static java.lang.String.format;
import static org.bonitasoft.web.designer.model.data.DataType.URL;

import org.bonitasoft.web.designer.model.data.Data;

public class TaskData implements PageData {

    public static final String NAME = "task";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Data create() {
        return new Data(URL, format("../API/bpm/userTask/{{%s}}", TaskIdData.NAME));
    }
}
