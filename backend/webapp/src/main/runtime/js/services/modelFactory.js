(function() {
  'use strict';

  function modelFactory($interpolate, $http, $log, $location) {

    function resolveVariable(dataModel, descriptor, name) {
      dataModel[name] = descriptor.value || undefined;
    }

    function resolveJson(dataModel, descriptor, name) {
      dataModel[name] = JSON.parse(descriptor.value);
    }

    function resolveExpression(dataModel, descriptor, name) {

      // use strict. Avoid pollution of the global object.
      /* jshint evil:true*/
      var expression = new Function('$data', '"use strict";' + descriptor.value), currentValue;

      Object.defineProperty(dataModel, name, {
        get: function() {
          try {
            var value = expression(dataModel);
            if (!angular.equals(currentValue, value)) {
              currentValue = value;
            }
            return currentValue;
          } catch (e) {
            $log.warn('Error evaluating <', name, '> data: ', e.message);
          }
        },
        enumerable: true
      });
    }

    function resolveUrl(dataModel, descriptor, name) {
      var url = null;
      var value;
      var csrf = {
        get promise() {
          return this.$promise || (this.$promise = $http({
            method: 'GET',
            url: '../API/system/session/unusedId'
          }).success(function() {
            $http.defaults.xsrfHeaderName = $http.defaults.xsrfCookieName = 'X-Bonita-API-Token';
          }));
        }
      };

      Object.defineProperty(dataModel, name, {
        get: function() {
          var currentUrl = $interpolate(descriptor.value, false, null, true)(dataModel);

          if (currentUrl !== url && currentUrl !== undefined) {
            url = currentUrl;
            csrf.promise.finally(function() {
              $http.get(url).success(function(data) {
                value = data;
              });
            });
          }
          return value;
        },
        enumerable: true
      });
    }

    function resolveUrlParameter(dataModel, descriptor, name) {
      function extractUrlParameter(param, str) {
        return decodeURI(str.replace(new RegExp('^(?:.*[&\\?]' + encodeURI(param).replace(/[\.\+\*]/g, '\\$&') + '(?:\\=([^&]*))?)?.*$', 'i'), '$1'));
      }

      Object.defineProperty(dataModel, name, {
        get: function() {
          return extractUrlParameter(descriptor.value || '', $location.absUrl());
        },
        enumerable: true
      });
    }

    var resolveMap = {
      variable: resolveVariable,
      constant: resolveVariable,
      json: resolveJson,
      expression: resolveExpression,
      url: resolveUrl,
      urlparameter: resolveUrlParameter
    };

    return {
      create: function(data) {

        var model = Object.keys(data).reduce(function(acc, name) {
          var descriptor = data[name];
          resolveMap[descriptor.type](acc, descriptor, name);
          return acc;
        }, {});

        model.createGateway = function() {
          var context = {};
          Object.keys(model).forEach(function(property) {
            Object.defineProperty(context, property, {
              get: function() {
                return model[property];
              },
              set: function(value) {
                model[property] = value;
              },
              enumerable: true
            });
          });
          return context;
        };
        return model;
      }
    };
  }

  angular.module('bonitasoft.ui.services')
    .factory('modelFactory', modelFactory);
})();
