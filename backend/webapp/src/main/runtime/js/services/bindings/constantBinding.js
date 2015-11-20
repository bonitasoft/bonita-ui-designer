(function () {
  'use strict';

  angular.module('bonitasoft.ui.services').service('ConstantBinding', (Binding) => (

    class ConstantBinding extends Binding {

      getValue() {
        return this.property.value;
      }
    }
  ));
})();
