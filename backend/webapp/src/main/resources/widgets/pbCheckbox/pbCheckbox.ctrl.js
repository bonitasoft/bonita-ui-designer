function PbCheckboxCtrl($scope, widgetNameFactory) {

  $scope.$watch('properties.value', function(value) {
    if (value === 'true' || value === true) {
      $scope.properties.value = true;
    } else {
      $scope.properties.value = false;
    }
  });

  this.name = widgetNameFactory.getName('pbCheckbox');
}
