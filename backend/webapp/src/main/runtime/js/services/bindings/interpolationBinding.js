(function () {
  'use strict';

  angular.module('bonitasoft.ui.services').service('InterpolationBinding', (Binding, gettextCatalog) => (

    class InterpolationBinding extends Binding {

      getValue() {
        return gettextCatalog.getString(this.property.value || '', this.context);
      }
    }
  ));
})();
