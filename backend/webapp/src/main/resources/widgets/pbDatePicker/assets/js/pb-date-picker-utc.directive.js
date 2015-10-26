(function () {

  'use strict';

  angular
      .module('bonitasoft.ui.extensions')
      .directive('pbDatePickerUtc', pbDatePickerUtc);

  /**
   * Directive used by UI Designer provided DatePicker widget.
   *
   * This perform date manipulation to avoid applying browser timezone when selecting/displaying a date
   */
  function pbDatePickerUtc() {
    return {
      restrict: 'A',
      priority: 1,
      require: 'ngModel',
      link: function (scope, element, attrs, ngModel) {

        ngModel.$parsers.push(utcDateParser);
        ngModel.$formatters.push(utcDateFormatter);

        function utcDateParser(viewValue) {
          if (angular.isDate(viewValue)) {
            viewValue.setTime(Date.UTC(viewValue.getFullYear(), viewValue.getMonth(), viewValue.getDate(), 0, 0, 0, 0));
          }
          return viewValue;
        }

        function utcDateFormatter(modelValue) {
          var date = parseDate(modelValue);
          if (angular.isDate(date)) {
            date.setMinutes(date.getTimezoneOffset());  // forces the "local" version of the date to match the intended UTC date
          }
          return date;
        }

        /**
         * Parse a date according to string input value
         * @param value a string representing a date (could be an iso date, integer timemillis or string timemillis)
         * @returns a Date or undefined when value is not parsable
         */
        function parseDate(value) {
          if (!value) return undefined;
          var date = new Date(value);    // = time millis NUMBER OR iso STRING formatted date
          if (isNaN(date)) {
            date = new Date(parseInt(value)); // = time millis STRING formatted date
          }
          if (isNaN(date)) {     // non parseable date
            return undefined;
          }
          return date;
        }
      }
    };
  }

})();
