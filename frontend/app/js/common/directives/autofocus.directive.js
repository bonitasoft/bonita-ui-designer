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
angular.module('bonitasoft.designer.common.directives')
  .directive('autofocus', function($timeout) {

    'use strict';

    // Because autofocus does not work as expected with angular :/
    return {
      restrict: 'A',
      require: '?aceEditor',
      link: function(scope, element, attr, ctrl) {
        var input = element.get(0);
        if (attr.autofocus) {
          // If the previous state was false and new one is true focus dat input
          scope.$watch(attr.autofocus, function(newVal, oldVal) {
            if (!oldVal && newVal) {
              // To be trigger after the watch...
              $timeout(function() {
                input.focus();
                if (input.type === 'text' && input.value.length > 0) {
                  input.select();
                }
              });
            }
          });
        }

        // If you do not depend of a property to watch as the defautl autofocus works only once with angular
        if (!attr.autofocus) {

          $timeout(function() {
            if (ctrl) {
              ctrl.editor.focus();
            } else {
              input.focus();
              if (input.type === 'text' && input.value.length > 0) {
                input.select();
              }
            }
          });
        }
      }
    };

  });
