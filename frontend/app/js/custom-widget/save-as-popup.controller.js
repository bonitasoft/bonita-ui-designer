(function() {

  'use strict';

  let _$modalInstance;

  class saveWidgetAsPopUpCtrl {

    constructor($modalInstance, widget) {
      this.widget = widget;
      this.newName = widget.name;
      _$modalInstance = $modalInstance;
    }

    ok() {
      let widget = angular.copy(this.widget);   // copy widget to avoid side effects in case of creation error
      widget.name = this.newName;
      _$modalInstance.close(widget);
    }
  }

  angular
      .module('bonitasoft.designer.custom-widget')
      .controller('saveWidgetAsPopUpCtrl', saveWidgetAsPopUpCtrl);

})();
