function PbDatePickerCtrl($scope, $log) {

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

    if($scope.properties.value===null){
      $log.warn('Your date picker need a data to be bound. Without data, your app doesn\'t know what to do with the date');
    }

  };
}
