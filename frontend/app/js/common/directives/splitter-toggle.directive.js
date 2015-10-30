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
 * add click event to element to trigger event to toggle a sidebar
 */
angular
  .module('bonitasoft.designer.common.directives')
  .directive('splitterToggle', function() {

    /**
     * Get event name according to target splitter type
     * @param {Element} splitter  targetted splitter
     * @returns {string}  the corresponding event name to trigger
     */
    function getEventName(splitter) {
      if (splitter.hasAttribute('splitter-horizontal')) {
        return 'splitter:toggle:bottom';
      } else if (splitter.hasAttribute('splitter-vertical')) {
        return splitter.getAttribute('splitter-vertical') === 'left' ? 'splitter:toggle:left' : 'splitter:toggle:right';
      } else {
        throw 'splitterToggle can only be applied to splitterHorizontal and splitterVertical';
      }
    }

    return {
      link: function($scope, $element, $attrs) {
        var eventName = getEventName(document.querySelector($attrs.splitterToggle));
        $element.on('click', function() {
          angular.element($attrs.splitterToggle).trigger(eventName, $attrs.targetState);
          $scope.$digest();
        });
      }
    };
  });
