function PbDateTimePickerCtrl($scope, $log, widgetNameFactory, $element, $locale, $bsDatepicker, moment) {

  'use strict';

  this.name = widgetNameFactory.getName('pbDateTimepicker');
  this.firstDayOfWeek = ($locale && $locale.DATETIME_FORMATS && $locale.DATETIME_FORMATS.FIRSTDAYOFWEEK) || 0;

  $bsDatepicker.defaults.keyboard = false;

  this.setDateAndTimeToNow = function() {
    var now = new moment();
    now.minute(Math.round(now.minute() / 5) * 5).second(0);
    this.formatMoment(now);
  };

  this.setDateToToday = function() {
    var now;
    if ($scope.properties.value) {
      // Set the date at today but don't change time
      var input = new moment($scope.properties.value);
      now = new moment({hour: input.hours(), minute: input.minutes(), seconds: input.seconds()});
    } else {
      now = new moment({hour: 0, minute: 0, seconds: 0})
    }
    this.formatMoment(now);
  };

  this.formatMoment = function(moment) {
    var isoFormat = 'YYYY-MM-DDTHH:mm:ss';
    if ($scope.properties.withTimeZone) {
      $scope.properties.value = moment.utc().format(isoFormat) + 'Z';
    } else {
      $scope.properties.value = moment.format(isoFormat);
    }
  };

  this.openDatePicker = function() {
    $element.find('input')[0].focus();
  };

  this.openTimePicker = function() {
    $element.find('input')[1].focus();
  };

  if (!$scope.properties.isBound('value')) {
    $log.error('the pbDateTimepicker property named "value" need to be bound to a variable');
  }
}
