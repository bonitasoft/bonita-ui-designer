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
 * directive used to validate that a page control contains valid JSON.
 * Usage: <textarea ng-model="some.property" valid-json></textarea>
 */
angular.module('bonitasoft.designer.editor.bottom-panel.data-panel').directive('exposableData', function() {

  'use strict';

  return {
    restrict: 'E',
    scope: {
      model: '='
    },
    templateUrl: 'js/editor/bottom-panel/data-panel/exposable-data.html'
  };
});
