function PbRadioBoxCtrl($scope, $parse, widgetNameFactory) {

  'use strict';
  var ctrl = this;
  /**
   * Watch the data source and set wrapChoices and $scope.properties.values
   */
  function comparator(initialValue, item) {
    return angular.equals(initialValue, ctrl.getValue(item));
  }

  function createGetter(accessor) {
    return accessor && $parse(accessor);
  }

  this.getLabel = createGetter($scope.properties.displayedKey) || function (item) {
    return typeof item === 'string' ? item : JSON.stringify(item);
  };
  this.getValue = createGetter($scope.properties.returnedKey) || function (item) {
    return item;
  };

  $scope.$watch('properties.availableValues', function(items){
    if (Array.isArray(items)) {
      $scope.properties.selectedValue = items
        .filter(comparator.bind(null, $scope.properties.selectedValue))
        .reduce(function (acc, item) {
          return ctrl.getValue(item);
        }, undefined);
    }
  });

  this.name = widgetNameFactory.getName('pbRadioBox');
}
