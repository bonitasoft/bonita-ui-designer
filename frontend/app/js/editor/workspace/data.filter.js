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
/**
 * Filter a data value to print data:{{data.value}} when data type is data
 * Just a reminder to the user in the editor that he has linked a field to a data
 */
angular.module('bonitasoft.ui.filters').filter('data', function () {

  'use strict';

  return function (param, paramType) {
    if (!param || !param.value) {
      return '';
    } else if (param.type === 'data') {
      var value = 'data:' + param.value;

      // In case of collection property type, we force the property to Array
      // so it plays well with editor render
      if (paramType === 'collection') {
        return [value];
      }

      return value;
    } else {
      return param.value;
    }
  };
});
