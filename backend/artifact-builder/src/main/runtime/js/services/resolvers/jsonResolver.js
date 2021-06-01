(function() {
  'use strict';

  angular
    .module('bonitasoft.ui.services')
    .run(createJsonResolver);

  function createJsonResolver(Resolver, ResolverService) {
    class JsonResolver extends Resolver {
      resolve() {
        this.model[this.name] = JSON.parse(this.content);
      }
    }

    ResolverService.addResolverType('json', (model, name, content) => new JsonResolver(model, name, content));
  }
})();
