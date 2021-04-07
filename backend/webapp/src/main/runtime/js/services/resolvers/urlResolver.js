(function () {
  'use strict';

  angular
    .module('bonitasoft.ui.services')
    .run(createUrlResolver);

  function createUrlResolver(Resolver, ResolverService, $http, $interpolate, $rootScope) {
    class UrlResolver extends Resolver {
      interpolateUrl() {
        return $interpolate(this.content, false, null, true)(this.model);
      }

      processResponse(response) {
        if (this.advancedOptions) {
          this.processAdvancedOptions(response);
        }
        this.model[this.name] = response.data;
        return this.model[this.name];
      }

      processAdvancedOptions(response) {
        let tmpAdvancedOptions = {};
        if (this.advancedOptions.headers) {
          tmpAdvancedOptions[this.advancedOptions.headers] = response.headers();
        }

        if (this.advancedOptions.statusCode) {
          tmpAdvancedOptions[this.advancedOptions.statusCode] = response.status;
        }

        // Push or set value of each model key
        let unFlattenedOptions = this.unflattenArray(tmpAdvancedOptions);
        for (var key in unFlattenedOptions) {
          if(typeof this.model[key]  === 'object'){
            Object.assign(this.model[key], unFlattenedOptions[key]);
          } else {
            this.model[key] = unFlattenedOptions[key];
          }
        }
      }

      /**
       * Convert entry data with flat key (key contains '.') into a real object with depth then returns this object
       * @example entry object like {data.headers: "myValue", data.secondLevel.code : "400" } will give you
       * {
       *  data : {
       *   headers: "myValue",
       *   secondLevel : {
       *     code: 400
       *   }
       *  }
       * }
       * @param Object Object to process to unflatten
       * @return Object a depth object
       */
      unflattenArray(data) {
        let result = {};
        for (var i in data) {
          let keys = i.split('.');
          keys.reduce(function (r, e, j) {
            return r[e] || (r[e] = isNaN(Number(keys[j + 1])) ? (keys.length - 1 === j ? data[i] : {}) : []);
          }, result)
        }
        return result;
      }

      resolve() {
        let url = this.interpolateUrl();
        if (angular.isDefined(url)) {
          return $http.get(url).then((data) => {
            return this.processResponse(data);
          }).catch((data) => {
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

    ResolverService.addResolverType('url', (model, name, content, advancedOptions) => new UrlResolver(model, name, content, advancedOptions));
  }
})();
