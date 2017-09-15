(function() {

  'use strict';

  class ExportPopUpController {
    constructor($uibModalInstance, page) {
      this.$uibModalInstance = $uibModalInstance;
      this.page = page;
      this.withJSON = true;
      this.exportMode = 'minified';
    }

    ok() {
      this.$uibModalInstance.close({ exportMode: this.exportMode, withJSON: this.withJSON });
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('ExportPopUpController', ExportPopUpController);
})();
