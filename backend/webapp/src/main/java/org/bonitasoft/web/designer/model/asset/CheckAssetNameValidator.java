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
package org.bonitasoft.web.designer.model.asset;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

public class CheckAssetNameValidator implements ConstraintValidator<CheckAssetName, String> {

    @Override
    public void initialize(CheckAssetName checkAssetName) {
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isEmpty(name)) {
            return true;
        }

        //The name can be an URL
        if(isAssetexternal(name)) {
            try {
                new URL(name);
                return true;
            } catch (MalformedURLException e) {
                return false;
            }
        }
        //The filename of local asset can't be changed by the user. The name is always valid because it's a filename validated by the user OS
        return true;
    }

    public static boolean isAssetexternal(String name){
        return name != null && (name.startsWith("http:") || name.startsWith("https:"));
    }
}
