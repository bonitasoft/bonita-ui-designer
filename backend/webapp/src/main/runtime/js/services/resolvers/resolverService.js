(function () {
  'use strict';

  class ResolverService {
    constructor() {
      this.resolvers = {};
    }

    addResolverType(name, Resolver) {
      this.resolvers[name] = Resolver;
    }

    createResolver(model, name, data) {
      return this.resolvers[data.type](model, name, data.displayValue, data.advancedOptions);
    }
  }

  angular
    .module('bonitasoft.ui.services')
    .service('ResolverService', () => new ResolverService());
})();
