(function () {
  'use strict';

  function modelFactory($interpolate, $http, $log, $location) {

    function resolveVariable(dataModel, descriptor, name) {
      dataModel[name] = descriptor.value||undefined;
    }

    function resolveJson(dataModel, descriptor, name) {
      dataModel[name] = JSON.parse(descriptor.value);
    }

    function resolveExpression(dataModel, descriptor, name) {

      // use strict. Avoid pollution of the global object.
      var expression = new Function('$data', '"use strict";' + descriptor.value);

      Object.defineProperty(dataModel, name, {
        get: function () {
          try {
            return expression(dataModel);
          } catch (e) {
            $log.warn("Error evaluating <", name, "> data: ", e.message);
          }
        },
        enumerable: true
      });
    }

    function resolveUrl(dataModel, descriptor, name) {

      var url = null;
      var value;

      Object.defineProperty(dataModel, name, {
        get: function () {
          var currentUrl = $interpolate(descriptor.value, false, null, true)(dataModel);

          if (currentUrl !== url && currentUrl !== undefined) {
            url = currentUrl;
            $http.get(url).success(function (data) {
              value = data;
            });
          }
          return value;
        },
        enumerable: true
      });
    }

    function resolveUrlParameter(dataModel, descriptor, name) {
      function extractUrlParameter(param, str) {
        return decodeURI(str.replace(new RegExp("^(?:.*[&\\?]" + encodeURI(param).replace(/[\.\+\*]/g, "\\$&") + "(?:\\=([^&]*))?)?.*$", "i"), "$1"));
      }

      Object.defineProperty(dataModel, name, {
        get: function () {
          return extractUrlParameter(descriptor.value || '', $location.absUrl());
        },
        enumerable: true
      });
    }

    return {
      create: function (data) {
        var model = _.transform(data, function (dataModel, descriptor, name) {
          ({
            variable: resolveVariable,
            constant: resolveVariable,
            json: resolveJson,
            expression: resolveExpression,
            url: resolveUrl,
            urlparameter: resolveUrlParameter
          }[descriptor.type] || angular.noop)(dataModel, descriptor, name);
        });
        model.createGateway = function () {
          var context = {};
          Object.keys(model).forEach(function (property) {
            Object.defineProperty(context, property, {
              get: function () {
                return model[property];
              },
              set: function (value) {
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

  angular.module('org.bonitasoft.pagebuilder.generator.services')
    .factory('modelFactory', modelFactory);
})();
