(function () {
  'use strict';

  angular.module('bonitasoft.ui.services').service('VariableBinding', (Binding, $parse) => (

    class VariableBinding extends Binding {

      constructor(property, context) {
        super(property, context);
        this.getter = $parse(property.value);
        this.isBound = !property.value;
      }

      getValue() {
        return (!this.isBound) ? this.getter(this.context) : this.value;
      }

      setValue(value) {
        return (!this.isBound) ? this.getter.assign(this.context, value) : (this.value = value);
      }
    }
  ));
})();
