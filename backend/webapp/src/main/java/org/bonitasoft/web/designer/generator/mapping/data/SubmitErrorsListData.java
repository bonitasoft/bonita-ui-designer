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

import static org.bonitasoft.web.designer.model.data.DataType.EXPRESSION;

import org.bonitasoft.web.designer.model.data.Data;

public class SubmitErrorsListData implements PageData {

    public static final String NAME = "submit_errors_list";
    public static final String SUBMIT_ERROR_DATA = String.format("%s._submitError", FormOutputData.NAME);

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Data create() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("if($data.%s && $data.%s && $data.%s.explanations){",FormOutputData.NAME,SUBMIT_ERROR_DATA,SUBMIT_ERROR_DATA));
        sb.append("\n\t");
        sb.append(String.format("const liElements = $data.%s.explanations",SUBMIT_ERROR_DATA));
        sb.append("\n\t\t");
        sb.append(".filter(cause => cause !== null)");
        sb.append("\n\t\t");
        sb.append(".map(cause => \"<li>\" + cause + \"</li>\")");
        sb.append("\n\t\t");
        sb.append(".join('');");
        sb.append("\n\t");
        sb.append("if(liElements){");
        sb.append("\n\t\t");
        sb.append("return \"<ul>\" + liElements + \"</ul>\";");
        sb.append("\n\t");
        sb.append("}");
        sb.append("\n");
        sb.append("}");
        return new Data(EXPRESSION,sb.toString());
    }
}
