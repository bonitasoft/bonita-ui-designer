(function() {
  'use strict';

  angular.module('bonitasoft.ui.services').service('ExpressionBinding', (Binding, $parse) => (

    class ExpressionBinding extends Binding {

      constructor(property, context) {
        super(property, context);
        this.getter = $parse(property.value);
      }

      getValue() {
        const newValue = this.getter(this.context);
        if (!angular.equals(this.currentValue, newValue)) {
          this.currentValue = newValue;
        }
        return this.currentValue;
      }
    }
  ));
})();
