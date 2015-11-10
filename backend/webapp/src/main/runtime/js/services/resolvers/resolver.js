(function() {
  'use strict';
  angular
    .module('bonitasoft.ui.services')
    .service('Resolver', createResolver);

  function createResolver() {

    class Resolver {
      constructor(model, name, content) {
        this.content = content;
        this.name = name;
        this.model = model;
      }
      resolve() {}
      watchDependencies() {}
      hasDependencies() { return false; }
    }
    return Resolver;
  }
})();
