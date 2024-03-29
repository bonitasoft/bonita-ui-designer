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
 * Component is an element directive allowing to display a widget component in the page editor.
 * It wraps the widget in a div containing also an overlay that will be displayed whenever the user
 * enter the div with his mouse, and hidden when he left.
 */
angular.module('bonitasoft.designer.editor.whiteboard').directive('component', function($compile, componentScopeBuilder) {

  'use strict';

  return {
    restrict: 'E',
    scope: {
      component: '=',
      editor: '=',
      componentIndex: '=',
      container: '=',
      row: '='
    },
    link: function(scope, element) {
      var componentScope = componentScopeBuilder.build(scope);

      if (scope.component.$$widget) {
        // insert the html template in the div with class widget-content
        var div = angular.element(element.get(0).querySelector('.widget-content'));

        if(scope.component.id === 'pbSelect'){
          // Ugly Hack to set internalValue to avoid displaying undefined in the whiteboard
          // when an available values
          // as it is the bound variable in this widget, this variable must be undefined
          // in the widget implementation
		      componentScope.ctrl= { internalValue : '' };
        }

        var widgetDomElement = $compile(scope.component.$$widget.template)(componentScope);
        div.append(widgetDomElement);
      }

    },
    templateUrl: 'js/editor/whiteboard/component.html'
  };
});
