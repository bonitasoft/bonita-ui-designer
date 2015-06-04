(function () {
  'use strict';

  angular.module('pb.generator.directives')
    .directive('pbDatepickerUtc', function ($parse, $filter) {
      function datepickerUtcLink(scope, element, attr, ngModelController) {

          // called with a JavaScript Date object when picked from the datepicker
          ngModelController.$parsers.push(function (viewValue) {
            if (angular.isDate(viewValue)) {
              viewValue.setMinutes(viewValue.getMinutes() - viewValue.getTimezoneOffset());
            }
            return viewValue;
          });

          // called with a 'yyyy-mm-dd' string to format
          ngModelController.$formatters.push(function (modelValue) {
            return $filter('date')( modelValue , attr.datepickerPopup);
          });
      }

      return  {
          restrict: 'A',
          require:  'ngModel',
          link: datepickerUtcLink
      };

    });
})();
