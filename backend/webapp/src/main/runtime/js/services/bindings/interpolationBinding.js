(function () {
  'use strict';

  angular.module('bonitasoft.ui.services').service('InterpolationBinding', (Binding, $interpolate) => (

    class InterpolationBinding extends Binding {

      getValue() {
        return $interpolate(this.property.value || '')(this.context);
      }
    }
  ));
})();
