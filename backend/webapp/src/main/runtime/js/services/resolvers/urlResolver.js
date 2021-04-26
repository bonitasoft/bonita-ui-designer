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

      resolve() {
        let url = this.interpolateUrl();
        if (angular.isDefined(url)) {
          return $http.get(url).then((data) => {
            return this.processResponse(data, false);
          }).catch((data) => {
            return this.processResponse(data, true);
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

      processResponse(response, isOnError) {
        if (this.advancedOptions) {
          this.processAdvancedOptions(response, isOnError);
        }
        if(!isOnError){
          this.model[this.name] = response.data;
        }
        return this.model[this.name];
      }

      processAdvancedOptions(response, isOnError) {
        let tmpAdvancedOptions = {};
        if (this.advancedOptions.headers) {
          let headers = {};
          let responseHeaders = response.headers();
          // Convert key as String to avoid key with '-' character
          Object.keys(responseHeaders).forEach(key => {
            headers[[key]] = responseHeaders[key];
          });
          tmpAdvancedOptions[this.advancedOptions.headers] = headers;
        }

        if (this.advancedOptions.statusCode) {
          tmpAdvancedOptions[this.advancedOptions.statusCode] = response.status;
        }

        if(this.advancedOptions.failedResponseValue){
          tmpAdvancedOptions[this.advancedOptions.failedResponseValue] = isOnError ? response.data : undefined;
        }

        // Push or set value of each model key
        let unFlattenedOptions = this.unflattenArray(tmpAdvancedOptions);
        for (var key in unFlattenedOptions) {
          if(typeof this.model[key]  === 'object'){
            Object.assign(this.model[[key]], unFlattenedOptions[key]);
          } else {
            this.model[[key]] = unFlattenedOptions[key];
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
    }

    ResolverService.addResolverType('url', (model, name, content, advancedOptions) => new UrlResolver(model, name, content, advancedOptions));
  }
})();
