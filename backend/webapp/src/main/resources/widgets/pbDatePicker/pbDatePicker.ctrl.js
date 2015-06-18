function PbDatePickerCtrl($scope, $log, widgetNameFactory) {

  'use strict';

  //By default the picker is closed
  this.opened = false;

  this.datepickerOptions = {
    startingDay: 1
  };

  this.today = function() {
    var date = new Date();
    $scope.properties.value = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
  };

  this.open = function ($event) {
    $event.preventDefault();
    $event.stopPropagation();
    this.opened = true;
  };

  this.name = widgetNameFactory.getName('pbDatepicker');

  if (!$scope.properties.isBound('value')) {
    $log.error('the pbDatepicker property named "value" need to be bound to a variable');
  }
}
