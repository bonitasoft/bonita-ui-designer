/**
 * directive used to validate that a page control contains valid JSON.
 * Usage: <textarea ng-model="some.property" valid-json></textarea>
 */
angular.module('pb.directives').directive('validJson', function() {

  'use strict';

  return {
    require: 'ngModel',
    link: function(scope, elm, attrs, ctrl) {
      ctrl.$validators.validJson = function(modelValue, viewValue) {
        if (ctrl.$isEmpty(modelValue)) {
          // consider empty models to be valid
          return true;
        }

        try {
          var val = angular.fromJson(viewValue);
          return angular.isObject(val) || angular.isArray(val);
        }
        catch (error) {
          return false;
        }
      };
    }
  };
});
