(function () {
  'use strict';

  angular.module('bonitasoft.ui.directives')
    .directive('pbDatepickerUtc', function ($parse, $filter) {
      function datepickerUtcLink(scope, element, attr, ngModelController) {
          // called with a JavaScript Date object when picked from the datepicker
          ngModelController.$parsers.push(function (viewValue) {
            if (angular.isDate(viewValue)) {
              // reset hours, min, sec...
              viewValue.setUTCHours(0,0,0,0);
            }
            return viewValue;
          });

          // called with a 'yyyy-mm-dd' string to format
          ngModelController.$formatters.push(function (modelValue) {
            if (needDateConversion(modelValue)) {
              var date = new Date(modelValue);
              date.setUTCHours(0,0,0,0);
              ngModelController.$setViewValue(date);
              return $filter('date')( date , attr.datepickerPopup);
            }
            return $filter('date')( modelValue , attr.datepickerPopup);
          });

      }

      function needDateConversion(value) {
        var val = Number(value);
        return Math.floor(val) === val;
      }

      return  {
          restrict: 'A',
          require:  'ngModel',
          link: datepickerUtcLink
      };

    });
})();
