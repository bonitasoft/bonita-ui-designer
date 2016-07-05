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
angular.module('bonitasoft.designer.editor.properties-panel').directive('propertyField', function($timeout) {

  'use strict';

  return {
    restrict: 'E',
    scope: {
      property: '=',
      propertyValue: '=',
      properties: '=',
      pageData: '='
    },
    templateUrl: 'js/editor/properties-panel/property-field.html',
    controller: 'PropertyFieldDirectiveCtrl',
    controllerAs: 'propertyField',
    link: function(scope, element) {
      scope.focusInput = function() {
        $timeout(() => {
          const input = element.find('input');
          const value = input.val();
          //force IE & FF to focus at the end of input
          input.focus().val('').val(value);
        }, 0);
      };
    }
  };
});
