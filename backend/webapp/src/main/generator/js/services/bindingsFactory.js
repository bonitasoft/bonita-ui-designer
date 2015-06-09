(function () {
  'use strict';

  /**
   * Factory to create bindings associating properties (also known as properties) to the model.
   *
   * @param $interpolate
   * @param $parse
   * @param $log
   * @returns {{create: createBindings}}
   */
  function bindingsFactory($interpolate, $parse, $log) {

    /**
     * Define destination properties allowing to
     * access the context object as described in properties.
     *
     * @param properties - also known as properties. { type: <data | constant>, value: <expression> }
     * @param context - against which property.value expression will be executed.
     * @param destination - object where to bind the resulting properties.
     */
    function createBindings(properties, context, destination) {

      /**
       * Allow accessing a data in a two way data binding matter.
       *
       * @param property
       * @returns {{get: Function, set: Function, enumerable: boolean}}
       */
      function createDataAccessors(property) {
        var getter = $parse(property);
        return {
          get: function () {
            return getter(context);
          },
          set: function (value) {
            getter.assign(context, value);
          },
          enumerable: true
        }
      }

      /**
       * Allow accessing a constant using angular interpolation feature. Update the value back is not allowed.
       *
       * @param value
       * @param name
       * @returns {{get: Function, set: Function, enumerable: boolean}}
       */
      function createConstantAccessors(value, name) {
        return {
          get: function () {
            return angular.isString(value) ? $interpolate(String(value))(context) : value;
          },
          set: function () {
            $log.warn('<', name, '> is a constant therefor it can\'t be updated. Please bind a data instead.');
          },
          enumerable: true
        }
      }

      var propertyAccessors = {
        // We need two way data binding here.
        'data': createDataAccessors,
        // All constants are expressions candidate for interpolation.
        // This is one way expression. We can't set an expression value back.
        'constant': createConstantAccessors
      };

      Object.keys(properties).forEach(function (name) {
        var property = properties[name];
        Object.defineProperty(destination, name, propertyAccessors[property.type](property.value, name));
      });
    }

    return {
      create: createBindings
    }
  }

  angular.module('pb.generator.services')
    .factory('bindingsFactory', bindingsFactory);
})();
