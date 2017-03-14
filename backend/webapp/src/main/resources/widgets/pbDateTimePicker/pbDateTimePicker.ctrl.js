function PbDateTimePickerCtrl($scope, $log, widgetNameFactory, $element, $locale, $bsDatepicker, moment) {

  'use strict';

  this.name = widgetNameFactory.getName('pbDateTimepicker');
  this.firstDayOfWeek = ($locale && $locale.DATETIME_FORMATS && $locale.DATETIME_FORMATS.FIRSTDAYOFWEEK) || 0;

  $bsDatepicker.defaults.keyboard = false;

  this.setDateAndTimeToNow = function() {
    var isoFormat = 'YYYY-MM-DDTHH:mm:ss';
    if ($scope.properties.withTimeZone) {
      var now = new moment().utc();
      $scope.properties.value = now.format(isoFormat) + 'Z';
    } else {
      var now = new moment();
      $scope.properties.value = now.format(isoFormat);
    }
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
