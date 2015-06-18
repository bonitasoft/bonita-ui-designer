function PbSelectCtrl($scope, $parse, $log, widgetNameFactory, $timeout) {
  var ctrl = this;

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

  $scope.$watch('properties.availableValues', function(items) {
    if (Array.isArray(items)) {
      var foundItem = items
        .filter(comparator.bind(null, $scope.properties.value))
        .reduce(function (acc, item) {
          return ctrl.getValue(item);
        }, undefined);

      // terrible hack to force the select ui to show the correct options
      // so we change it's value to undefined and then delay to the correct value
      if (foundItem) {
        $scope.properties.value = undefined;
      }
      $timeout(function(){
        if (foundItem) {
          $scope.properties.value = foundItem;
        }
      }, 0);
    }

  });

  this.name = widgetNameFactory.getName('pbSelect');

  if (!$scope.properties.isBound('value')) {
    $log.error('the pbSelect property named "value" need to be bound to a variable');
  }
}
