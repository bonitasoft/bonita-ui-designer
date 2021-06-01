angular.module('bonitasoft.ui.services').factory('propertyValuesFactory', function() {
  var resources = {};
  resources['component-ref'] =  {"foo":{"type":"bar","value":"baz"}};
  return {
    get: function(uuid) {
      return resources[uuid];
    }
  };
});
