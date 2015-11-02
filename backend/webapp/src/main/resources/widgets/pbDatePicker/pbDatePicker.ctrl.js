function PbDatePickerCtrl($scope, $log, widgetNameFactory, $element) {

  'use strict';

  this.name = widgetNameFactory.getName('pbDatepicker');

  this.open = function () {
    angular.element($element).find('input').triggerHandler('click');
  };

  if (!$scope.properties.isBound('value')) {
    $log.error('the pbDatepicker property named "value" need to be bound to a variable');
  }


}
