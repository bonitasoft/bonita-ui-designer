(function() {
  'use strict';

  angular
    .module('bonitasoft.ui.services')
    .run(createUrlParameterResolver);

  function createUrlParameterResolver(Resolver, ResolverService, $location, $rootScope) {
    class UrlParameterResolver extends Resolver {
      resolve() {
        // Since decodeURIComponent does not decode '+', we have to replace '+' by '%20'
        function extractUrlParameter(param, str) {
          return decodeURIComponent(str.replace(/\+/g, '%20').replace(new RegExp('^(?:.*[&\\?]' + encodeURIComponent(param).replace(/[\.\+\*]/g, '\\$&') + '(?:\\=([^&]*))?)?.*$', 'i'), '$1'));
        }
        this.model[this.name] = extractUrlParameter(this.content || '', $location.absUrl());
      }
      watchDependencies() {
        $rootScope.$watch(() => $location.absUrl(), () => this.resolve());
      }
    }
    ResolverService.addResolverType('urlparameter', (model, name, content) => new UrlParameterResolver(model, name, content));
  }
})();
