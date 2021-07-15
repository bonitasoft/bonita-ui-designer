(function() {

  'use strict';

  class ExportPopUpController {
    constructor($scope, $uibModalInstance, keyBindingService, $localStorage, page) {
      'ngInject';
      this.$uibModalInstance = $uibModalInstance;
      this.$localStorage = $localStorage;
      this.page = page;
      this.doNotShowAgain = false;

      // Pause keyBindingService to avoid move selection in background when user press arrow key
      keyBindingService.pause();
      // Unpause keyBindingService when popup is destroy
      $scope.$on('$destroy', function() {
        keyBindingService.unpause();
      });
    }

    ok() {
      if (!this.$localStorage.bonitaUIDesigner) {
        this.$localStorage.bonitaUIDesigner = {};
      }
      this.$localStorage.bonitaUIDesigner.doNotShowExportMessageAgain = this.doNotShowAgain;
      this.$uibModalInstance.close();
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('ExportPopUpController', ExportPopUpController);
})();
