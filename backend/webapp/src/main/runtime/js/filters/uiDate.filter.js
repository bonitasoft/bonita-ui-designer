(function() {
  'use strict';

  angular.module('bonitasoft.ui.filters')

    .filter('uiDate', function($filter) {
      return function(date, format, timezone) {
        //if the value is a long, set the timezone to UTC to display the date
        if (!timezone && !isNaN(date) && isFinite(date)) {
          timezone = 'UTC';
        }
        //if the value is an ISO String that contains a time set a default format that contains the time
        //default angularJS format is mediumDate
        if (!format && date && typeof date === 'string' && date.length > 10) {
          format = 'medium';
        }
        return $filter('date')(date, format, timezone);
      };
    });
})();
