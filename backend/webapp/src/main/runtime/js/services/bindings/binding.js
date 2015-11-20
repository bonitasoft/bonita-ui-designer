(function () {
  'use strict';

  angular.module('bonitasoft.ui.services').service('Binding', () => (

    class Binding {

      constructor(property, context) {
        this.property = property;
        this.context = context;
      }
    }
  ));
})();
