(function() {

  'use strict';

  class SaveAsPopUpController {
    constructor($modalInstance, page) {
      this.$modalInstance = $modalInstance;
      this.page = page;
      this.newName = page.name;
    }

    ok() {
      let page = angular.copy(this.page);     // copy page to avoid side effects in case of creation error
      page.name = this.newName;
      this.$modalInstance.close(page);
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('SaveAsPopUpController', SaveAsPopUpController);
})();
