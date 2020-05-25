(function () {
  'use strict';

  angular
    .module('bonitasoft.ui.services')
    .run(createBusinessDataResolver);

  function createBusinessDataResolver(Resolver, ResolverService, $http, $interpolate, $rootScope) {
    class BusinessData extends Resolver {

      buildAPI() {
        let content = JSON.parse(this.content);
        let filters = '';
        content.filters.forEach(filter =>
          filters = filters + '&f=' + filter.name.concat('=', filter.value)
        );

        return `../API/bdm/businessData/${content.id}?q=${content.query.name}&p=${content.pagination.p}&c=${content.pagination.c}${filters}`;
      }

      interpolateUrl() {
        return $interpolate(this.buildAPI(), false, null, true)(this.model);
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
        return (this.buildAPI() || '').match(/\{\{[^\}]+\}\}/);
      }
    }

    ResolverService.addResolverType('businessdata', (model, name, content) => new BusinessData(model, name, content));
  }
})();
