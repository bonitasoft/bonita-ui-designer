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

      processResponse(response){
        let cloneData = Object.assign(response.data);
        cloneData.__headers = Object.assign({},response.headers());
        cloneData.__status = response.status;
        this.model[this.name] = cloneData;
        return this.model[this.name];
      }

      resolve() {
        let url = this.interpolateUrl();
        if (angular.isDefined(url)) {
          return $http.get(url).then((data) => {
            return this.processResponse(data);
          }).catch((data)=>{
            return this.processResponse(data);
          });
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
