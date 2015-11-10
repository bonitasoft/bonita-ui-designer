(function() {
  'use strict';

  angular
    .module('bonitasoft.ui.services')
    .run(createUrlParameterResolver);

  function createUrlParameterResolver(Resolver, ResolverService, $location) {
    class UrlParameterResolver extends Resolver {
      resolve() {
        function extractUrlParameter(param, str) {
          return decodeURI(str.replace(new RegExp('^(?:.*[&\\?]' + encodeURI(param).replace(/[\.\+\*]/g, '\\$&') + '(?:\\=([^&]*))?)?.*$', 'i'), '$1'));
        }
        this.model[this.name] = extractUrlParameter(this.content || '', $location.absUrl());
      }
    }
    ResolverService.addResolverType('urlparameter', (model, name, content) => new UrlParameterResolver(model, name, content));
  }
})();
