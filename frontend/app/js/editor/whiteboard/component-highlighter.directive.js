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
(function() {
  'use strict';

  angular.module('bonitasoft.designer.editor.whiteboard').directive('componentHighlighter', function() {
    return {
      restrict: 'A',
      link: function($scope, elem, attrs) {
        var cssClassName = attrs.componentHighlighter;
        var node;

        function onMouseOver(event) {
          var currentNode = event.target;
          while (currentNode.parentNode) {
            if (/\w-element/.test(currentNode.className)) {
              if (node === currentNode) {
                return;
              }
              if (node) {
                node.className = node.className.replace(cssClassName,'').trim();
              }
              currentNode.className += ' ' + cssClassName;
              node = currentNode;
              return;
            }
            currentNode = currentNode.parentNode;
          }
        }

        function onMouseLeave() {
          if (node) {
            node.className = node.className.replace(cssClassName,'');
            node = null;
          }
        }

        var wrapper = angular.element(elem[0].querySelector('.widget-wrapper'));

        elem.on('mouseover', onMouseOver);
        wrapper.on('mouseleave', onMouseLeave);
      }
    };
  });
})();
