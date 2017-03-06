function PbDateTimePickerCtrl($scope, $log, widgetNameFactory, $element, $locale, $bsDatepicker) {

  'use strict';

  this.name = widgetNameFactory.getName('pbDateTimepicker');
  this.firstDayOfWeek = ($locale && $locale.DATETIME_FORMATS && $locale.DATETIME_FORMATS.FIRSTDAYOFWEEK) || 0;
  this.timezone = $scope.properties.withTimeZone ? null : 'UTC';

  $bsDatepicker.defaults.keyboard = false;

  this.setDateAndTimeToNow = function() {
    var now = new Date();
    if (!$scope.properties.withTimeZone) {
      //We need to add this offset for the displayed date to be correct
      if (now.getTimezoneOffset() > 0) {
        now.setTime(now.getTime() + (now.getTimezoneOffset() * 60000));
      } else if (now.getTimezoneOffset() < 0) {
        now.setTime(now.getTime() - (now.getTimezoneOffset() * 60000));
      }
    }
    $scope.properties.value = now;
  };

  this.openDatePicker = function () {
    $element.find('input')[0].focus();
  };

  this.openTimePicker = function () {
    $element.find('input')[1].focus();
  };

  if (!$scope.properties.isBound('value')) {
    $log.error('the pbDateTimepicker property named "value" need to be bound to a variable');
  }


}
