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
 * Container is an element directive allowing to display a container in the page editor. The container is either
 * the top-lever one, or a sub-container contained in a parent container cell.
 */
angular.module('bonitasoft.designer.editor.whiteboard').directive('container', function(RecursionHelper) {

  'use strict';

  return {
    restrict: 'E',
    scope: {
      id: '@',
      container: '=',
      editor: '=',
      component: '='
    },
    templateUrl: 'js/editor/whiteboard/container.html',
    controller: 'ContainerDirectiveCtrl',
    compile: function(element) {
      return RecursionHelper.compile(element, function() {
      });
    }
  };
});
