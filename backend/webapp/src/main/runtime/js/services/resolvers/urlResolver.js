(function() {
  'use strict';

  angular
    .module('bonitasoft.ui.services')
    .run(createUrlResolver);

  function createUrlResolver(Resolver, ResolverService, $http, $interpolate, $rootScope) {
    var csrf = {
      get promise() {
        return this.$promise || (this.$promise = $http({
            method: 'GET',
            url: '../API/system/session/unusedId'
          }).success(() => $http.defaults.xsrfHeaderName = $http.defaults.xsrfCookieName = 'X-Bonita-API-Token'));
      }
    };

    class UrlResolver extends Resolver {

      interpolateUrl() {
        return $interpolate(this.content, false, null, true)(this.model);
      }

      resolve() {
        var url = this.interpolateUrl();
        if (angular.isDefined(url)) {
          return csrf.promise.finally(
            () => $http.get(url).success((data) => this.model[this.name] = data)
          );
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
