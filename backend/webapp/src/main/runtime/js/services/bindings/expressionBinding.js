(function () {
  'use strict';

  angular.module('bonitasoft.ui.services').service('ExpressionBinding', (Binding, $parse) => (

    class ExpressionBinding extends Binding {

      constructor(property, context) {
        super(property, context);
        this.getter = $parse(property.value);
      }

      getValue() {
        return this.getter(this.context);
      }
    }
  ));
})();
