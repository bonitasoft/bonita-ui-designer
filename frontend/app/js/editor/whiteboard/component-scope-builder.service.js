/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Softwœare Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Initialises a scope for component directive
 *
 * Create a new scope with a properties object derived from user entered propertyValues.
 * This allow to bind propertyValues to widget properties and keep a WYSWYG approach in editor while editing widget properties
 */
angular.module('bonitasoft.designer.editor.whiteboard').factory('componentScopeBuilder', function(dataFilter) {

  'use strict';

  var build = function(scope) {
    var componentScope = scope.$new(true);

    componentScope.environment = {};
    componentScope.environment.editor = {
      pageId: scope.editor && scope.editor.page && scope.editor.page.id
    };
    componentScope.environment.component = scope.component.$$widget;

    // Keep in sync propertyValues and injected properties in widget
    componentScope.properties = {};
    angular.forEach(scope.component.propertyValues, function(value, key) {
      scope.$watch('component.propertyValues["' + key + '"].value', function() {
        // we extract the corresponding property descriptor from widget (using filter)
        // in order to get its type  (using map)
        var propertyType = scope.component.$$widget.properties.filter((param) => param.name === key)
          .map((param) => param.type).pop() || 'text';

        componentScope.properties[key] = dataFilter(scope.component.propertyValues[key],  propertyType);
      });
    });

    // utility function, available in widget's template, to iterate over a range
    componentScope.range = (size) => new Array(size);

    return componentScope;
  };

  return {
    build: build
  };
});
