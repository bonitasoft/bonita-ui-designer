(function () {
  'use strict';

  angular.module('bonitasoft.ui.services').service('VariableBinding', (Binding, $parse) => (

    class VariableBinding extends Binding {

      constructor(property, context) {
        super(property, context);
        this.getter = $parse(property.value);
      }

      getValue() {
        return this.getter(this.context);
      }

      setValue(value) {
        return this.getter.assign && this.getter.assign(this.context, value);
      }
    }
  ));
})();
