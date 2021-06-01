(function() {
  'use strict';

  angular
    .module('bonitasoft.ui.services')
    .run(createConstantResolver);

  function createConstantResolver(Resolver, ResolverService) {

    class ConstantResolver extends Resolver {
      resolve() {
        this.model[this.name] = this.content || undefined;
      }
    }

    ResolverService.addResolverType('constant', (model, name, content) => new ConstantResolver(model, name, content));
    ResolverService.addResolverType('variable', (model, name, content) => new ConstantResolver(model, name, content));
  }
})();
