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
 * componentPreview is an element directive allowing to display a component in the preview.
 */
angular.module('bonitasoft.designer.editor.whiteboard').directive('componentPreview', function($compile, componentScopeBuilder) {

  'use strict';

  return {
    restrict: 'E',
    scope: {
      component: '='
    },
    replace: true,
    link: function(scope, element) {
      var componentScope = componentScopeBuilder.build(scope);

      // insert the html template in the div with class widget-content
      var widgetDomElement = $compile(scope.component.$$widget.template)(componentScope);
      element.append(widgetDomElement);
    },
    template: '<div class="widget-component"></div>'
  };
});
