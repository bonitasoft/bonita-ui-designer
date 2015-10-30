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
angular.module('bonitasoft.designer.editor.whiteboard')
  .directive('dropZone', function(componentUtils) {

    'use strict';

    return {
      template: '<div class="dropZone" bo-dropzone bo-drop-success="dropBefore($data)"></div>' +
      '<div class="dropZone dropZone--right" bo-dropzone bo-drop-success="dropAfter($data)"></div>',

      link: function(scope) {

        /**
         * Move an item inside a row
         * Test suite (we cannot test it right now)
         * [1,2,3]
         *
         * // Do not modify array
         * 1 dropBefore 1 =  1,2,3
         * 1 dropAfter 1 = 1,2,3
         * 1 dropBefore 2 = 1,2,3
         * 2 dropBefore 2 = 1,2,3
         * 2 dropAfter 2 = 1,2,3
         * 2 dropBefore 3 = 1,2,3
         * 2 dropAfter 1 = 1,2,3
         * 3 dropBefore 3 = 1,2,3
         * 3 dropAfter 3 = 1,2,3
         * 3 dropAfter 2 = 1,2,3
         *
         * // Modify order
         * 1 dropAfter 2 = 2,1,3
         * 1 dropBefore 3 = 2,1,3
         * 1 dropAfter 3 = 2,3,1
         * 2 dropBefore 1 = 2,1,3
         * 2 dropAfter 3 = 1,3,2
         * 3 dropBefore 1 = 3,1,2
         * 3 dropAfter 1 = 1,3,2
         * 3 dropBefore 2 = 1,3,2

         *
         * @param  {Object} data
         * @param  {Boolean} before is it a dropZone before ?
         * @return {void}
         */
        function movePosition(data, before) {

          var isWidgetNotAlreadyInTheRow = -1 === scope.row.indexOf(data),
            componentIndex = scope.componentIndex;

          // You cannot drop a container inside itself, nor in its children
          if (notAllowedToMoveContainer(data)) {
            return;
          }

          // Increment index for dropZone after for the first item only
          if (!componentIndex && !before) {
            componentIndex++;
          }

          if (isWidgetNotAlreadyInTheRow) {

            // Remove the old widget
            scope.editor.selectRow(scope.container, data.$$parentContainerRow.row);
            scope.editor.removeCurrentComponent(data);

            // Push the widget to another place
            scope.editor.selectRow(scope.container, scope.row);
            scope.editor.dropElement(data);

            if (scope.componentIndex && !before) {
              componentIndex++;
            }

          } else {

            var length = scope.row.length,
              indexItem = scope.row.indexOf(data);

            // Do nothing for current item
            if (indexItem === scope.componentIndex) {
              return;
            }

            // Drag and drop first item on its before dropZone
            if (before && indexItem === 0 && scope.componentIndex !== length - 1) {
              return;
            }

            // Take an item and drag it to the before next to it
            if (indexItem + 1 === scope.componentIndex && before) {
              return;
            }

            // Take an item and drag it to the after's element next to it
            if (indexItem - 1 === scope.componentIndex && !before) {
              return;
            }

            scope.editor.selectRow(scope.container, scope.row);

            // For the last item onDropBefore it should move left
            if (before && scope.componentIndex === length - 1) {
              componentIndex--;
            }
          }

          scope.editor.moveAtPosition(componentIndex, data);
        }

        /**
         * You cannot move a container into another one for:
         * - It's a container and it's the $scope.container too
         * - It's a container and it's the child of another one
         * @param  {Object} data Item to move
         * @return {Boolean}
         */
        function notAllowedToMoveContainer(data) {
          return !componentUtils.isMovable(data, scope.component);
        }

        /**
         * Drop an item to a custom position in a row
         * @param  {Object} data - Item to push in the row
         * @param  {Number} index - Position of the item
         * @return {void}
         */
        function dropAtPosition(data, index) {
          index = (typeof index === 'undefined') ? scope.componentIndex : index;
          scope.editor.addComponentToRow(data, scope.container, scope.row, index);
        }

        scope.dropBefore = function(data) {

          // If you are trying to dragAndDrop a widget already defined
          if (data.$$widget) {
            return movePosition(data, true);
          }
          dropAtPosition(data);
        };

        scope.dropAfter = function(data) {
          if (data.$$widget) {
            return movePosition(data);
          }
          dropAtPosition(data, scope.componentIndex + 1);
        };

      }
    };
  });
