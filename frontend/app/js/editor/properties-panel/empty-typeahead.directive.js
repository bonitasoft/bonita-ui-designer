angular.module('pb.directives')
  .directive('emptyTypeahead', function() {

  'use strict';

  return {
    require: 'ngModel',
    link: function(scope, element, attrs, modelCtrl) {
      element.bind('focus', function() {
        modelCtrl.$viewValue = modelCtrl.$viewValue || ' ';
        modelCtrl.$parsers.forEach(function(parser) {
          parser(modelCtrl.$viewValue);
        });
      });
    }
  };
});
