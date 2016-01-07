(function () {
  'use strict';

  angular.module('bonitasoft.ui.services').factory('bindingsFactory', (bindingService) => ({

    /**
     * Define destination properties allowing to
     * access the context object as described in properties.
     *
     * @param properties - also known as properties. { type: <data | constant>, value: <expression> }
     * @param context - against which property.value expression will be executed.
     * @param destination - object where to bind the resulting properties.
     */
    create(properties, context, destination) {

      Object.keys(properties).forEach(function (name) {
        var binding = bindingService.create(properties[name], context);
        Object.defineProperty(destination, name, {
          get: function () {
            return binding.getValue();
          },
          set: function (value) {
            return binding.setValue && binding.setValue(value);
          },
          enumerable: true
        });
      });
    }
  }));
})();
