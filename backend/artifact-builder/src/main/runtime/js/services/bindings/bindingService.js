(function () {
  'use strict';

  function capitalize(text) {
    return text.charAt(0).toUpperCase() + text.slice(1);
  }

  angular.module('bonitasoft.ui.services')
    .service('bindingService', ($injector) => ({

      create(property, context) {
        let Binding = $injector.get(capitalize(property.type) + 'Binding');
        return new Binding(property, context);
      }
    }));
})();
