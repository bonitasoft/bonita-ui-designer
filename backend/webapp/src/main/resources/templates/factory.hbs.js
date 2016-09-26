angular.module('bonitasoft.ui.services').factory('{{name}}Factory', function() {
  var resources = {};
  {{#each resources}}
  resources['{{ @key }}'] =  {{ json this }};
  {{/each}}
  return {
    get: function(uuid) {
      return resources[uuid];
    }
  };
});
