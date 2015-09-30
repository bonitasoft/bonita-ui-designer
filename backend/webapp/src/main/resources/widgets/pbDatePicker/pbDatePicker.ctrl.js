function PbDatePickerCtrl($scope, $log, widgetNameFactory) {

  'use strict';

  //By default the picker is closed
  this.opened = false;

  this.datepickerOptions = {
    startingDay: 1
  };

  this.today = function() {
    $scope.properties.value = new Date();
    this.floorDate();
  };

  this.open = function($event) {
    $event.preventDefault();
    $event.stopPropagation();
    this.opened = true;
  };

  this.floorDate = function() {
    if (angular.isDate($scope.properties.value)) {
      $scope.properties.value.setTime(Date.UTC($scope.properties.value.getFullYear(), $scope.properties.value.getMonth(), $scope.properties.value.getDate(), 0, 0, 0, 0));
    }
  };

  this.name = widgetNameFactory.getName('pbDatepicker');

  if (!$scope.properties.isBound('value')) {
    $log.error('the pbDatepicker property named "value" need to be bound to a variable');
  }
}
