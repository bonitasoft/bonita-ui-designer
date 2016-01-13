(function() {

  'use strict';

  class SaveAsPopUpController {
    constructor($uibModalInstance, page) {
      this.$uibModalInstance = $uibModalInstance;
      this.page = page;
      this.newName = page.name;
    }

    ok() {
      let page = angular.copy(this.page);     // copy page to avoid side effects in case of creation error
      page.name = this.newName;
      this.$uibModalInstance.close(page);
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('SaveAsPopUpController', SaveAsPopUpController);
})();
