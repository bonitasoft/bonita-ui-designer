function PbAutocompleteCtrl($scope, $parse, widgetNameFactory) {

  'use strict';

  function createGetter(accessor) {
    return accessor && $parse(accessor);
  }

  this.getLabel = createGetter($scope.properties.displayedKey) || function (item) {
    return typeof item === 'string' ? item : JSON.stringify(item);
  };

  this.name = widgetNameFactory.getName('pbAutocomplete');
}
