angular.module('pb.directives').directive('propertyField', function() {

  'use strict';

  return {
    restrict: 'E',
    scope: {
      property: '=',
      propertyValue: '=',
      properties: '=',
      pageData: '='
    },
    templateUrl: 'js/editor/properties-panel/property-field.html',
    controller: 'PropertyFieldDirectiveCtrl'
  };
});
