/**
 * Initialises a scope for component directive
 *
 * Create a new scope with a properties object derived from user entered propertyValues.
 * This allow to bind propertyValues to widget properties and keep a WYSWYG approach in editor while editing widget properties
 */
angular.module('pb.services').factory('componentScopeBuilder', function(dataFilter) {

  'use strict';

  var build = function(scope) {
    var componentScope = scope.$new(true);

    // Keep in sync propertyValues and injected properties in widget
    componentScope.properties = {};
    angular.forEach(scope.component.propertyValues, function(value, key) {
      scope.$watch('component.propertyValues["' + key + '"].value', function() {
        // we extract the corresponding property descriptor from widget (using filter)
        // in order to get its type  (using map)
        var propertyType = scope.component.$$widget.properties.filter(function(param){
            return param.name === key;
          })
          .map( function(param) {
            return param.type;
          }).pop() || 'text';

        componentScope.properties[key] = dataFilter(scope.component.propertyValues[key],  propertyType);
      });
    });

    // utility function, available in widget's template, to iterate over a range
    componentScope.range = function(size) {
      return new Array(size);
    };

    return componentScope;
  };

  return {
    build: build
  };
});
