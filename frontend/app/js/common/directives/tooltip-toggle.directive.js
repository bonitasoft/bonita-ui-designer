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
 * Wrap ace directive according to page builder needs
 */

angular.module('bonitasoft.designer.common.directives').directive('tooltipToggle', function($timeout) {
  'use strict';

  return {
    name: 'tooltipToggle',
    link: function(scope, element, attr) {
      attr.tooltipTrigger = 'show-tooltip';

      scope.$watch(attr.tooltipToggle, function(value) {
        if (value) {
          // tooltip provider will call scope.$apply, so need to get out of this digest cycle first
          $timeout(function() {
            element.triggerHandler('show-tooltip');
          });
        } else {
          $timeout(function() {
            element.triggerHandler('hide-tooltip');
          });
        }
      });
    }
  };
});
