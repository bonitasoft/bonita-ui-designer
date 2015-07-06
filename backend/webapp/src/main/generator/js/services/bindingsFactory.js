(function () {
  'use strict';

  /**
   * Factory to create bindings associating properties (also known as properties) to the model.
   *
   * @param $interpolate
   * @param $parse
   * @returns {{create: createBindings}}
   */
  function bindingsFactory($interpolate, $parse) {

    /**
     * Define destination properties allowing to
     * access the context object as described in properties.
     *
     * @param properties - also known as properties. { type: <data | constant>, value: <expression> }
     * @param context - against which property.value expression will be executed.
     * @param destination - object where to bind the resulting properties.
     */
    function createBindings(properties, context, destination) {

      function createVariable(propertyValue) {
        var getter = $parse(propertyValue);
        return {
          get: function () {
            return getter(context);
          },
          set: function (value) {
            if (getter.assign) {
              getter.assign(context, value);
            }
          },
          enumerable: true
        };
      }

      function createExpression(propertyValue) {
        var getter = $parse(propertyValue);
        return {
          get: function () {
            return getter(context);
          },
          enumerable: true
        }
      }

      function createInterpolation(propertyValue) {
        return {
          get: function () {
            return $interpolate(propertyValue || '')(context);
          },
          enumerable: true
        };
      }

      function createConstant(propertyValue) {
        return {
          get: function () {
            return propertyValue;
          },
          enumerable: true
        }
      }

      var bonds = {
        'variable': createVariable,
        'expression': createExpression,
        'interpolation': createInterpolation,
        'constant': createConstant
      };

      Object.keys(properties).forEach(function (name) {
        var property = properties[name];
        Object.defineProperty(destination, name, bonds[property.type](property.value, name));
      });
    }

    return {
      create: createBindings
    }
  }

  angular.module('pb.generator.services')
    .factory('bindingsFactory', bindingsFactory);
})();
