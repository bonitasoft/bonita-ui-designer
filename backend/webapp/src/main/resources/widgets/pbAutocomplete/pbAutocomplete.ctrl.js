function PbAutocompleteCtrl($scope, $parse, $log, widgetNameFactory) {

  'use strict';

  function createGetter(accessor) {
    return accessor && $parse(accessor);
  }

  this.getLabel = createGetter($scope.properties.displayedKey) || function (item) {
    return typeof item === 'string' ? item : JSON.stringify(item);
  };

  this.name = widgetNameFactory.getName('pbAutocomplete');

  if (!$scope.properties.isBound('value')) {
    $log.error('the pbAutocomplete property named "value" need to be bound to a variable');
  }
}
