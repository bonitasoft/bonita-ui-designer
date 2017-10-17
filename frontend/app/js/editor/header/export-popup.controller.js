(function() {

  'use strict';

  class ExportPopUpController {
    constructor($uibModalInstance, $localStorage, page) {
      'ngInject';
      this.$uibModalInstance = $uibModalInstance;
      this.$localStorage = $localStorage;
      this.page = page;
      this.doNotShowAgain = false;
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
