(function() {
  'use strict';

  angular
    .module('bonitasoft.ui.services')
    .run(createUrlResolver);

  function createUrlResolver(Resolver, ResolverService, $http, $interpolate, $rootScope) {
    class UrlResolver extends Resolver {

      interpolateUrl() {
        return $interpolate(this.content, false, null, true)(this.model);
      }

      resolve() {
        var url = this.interpolateUrl();
        if (angular.isDefined(url)) {
          return $http.get(url).success((data) => this.model[this.name] = data);
        }
      }
      watchDependencies() {
        if (this.hasDependencies()) {
          $rootScope.$watch(() => this.interpolateUrl(), () => this.resolve());
        }
      }
      hasDependencies() {
        //test if the url contains some {{ }} which shows that it has dependencies
        return (this.content || '').match(/\{\{[^\}]+\}\}/);
      }
    }
    ResolverService.addResolverType('url',(model, name, content) => new UrlResolver(model, name, content));
  }
})();
