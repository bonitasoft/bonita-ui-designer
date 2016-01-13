(function() {

  'use strict';

  let _$uibModalInstance;

  class saveWidgetAsPopUpCtrl {

    constructor($uibModalInstance, widget) {
      this.widget = widget;
      this.newName = widget.name;
      _$uibModalInstance = $uibModalInstance;
    }

    ok() {
      let widget = angular.copy(this.widget);   // copy widget to avoid side effects in case of creation error
      widget.name = this.newName;
      _$uibModalInstance.close(widget);
    }
  }

  angular
      .module('bonitasoft.designer.custom-widget')
      .controller('saveWidgetAsPopUpCtrl', saveWidgetAsPopUpCtrl);

})();
