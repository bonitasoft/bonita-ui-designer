function PbChecklistCtrl($scope, $parse) {

  'use strict';
  var ctrl = this;
  /**
   * Watch the data source and set wrapChoices and $scope.properties.selectedValues
   */
  function comparator(item, initialValue) {
    return angular.equals(item, initialValue);
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


  /**
   * update the scope.properties.selectedValues with the selected items
   */
  this.updateValues = function(index) {
    $scope.properties.selectedValues = ctrl.selectedItems
      .map(function(checked, index) {
        if (checked !== true) {
          return false;
        }
        var item =  $scope.properties.availableValues[index];
        return ctrl.getValue(item);
      }).filter(function(item){
        return item !== false;
      });
  };


  $scope.$watch('properties.availableValues', function(items){
    ctrl.selectedItems = (items || []).map(function(item){
      if ( Array.isArray($scope.properties.selectedValues)) {
        return $scope.properties.selectedValues.some( comparator.bind(null, ctrl.getValue(item)) );
      }
      return false;
    });

  });
}
