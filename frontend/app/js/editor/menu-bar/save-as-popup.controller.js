(function() {

  'use strict';

  let _$modalInstance;

  class SaveAsPopUpController {
    constructor($modalInstance, page) {
      _$modalInstance = $modalInstance;
      this.page = page;
      this.newName = page.name;
    }

    ok() {
      let page = angular.copy(this.page);     // copy page to avoid side effects in case of creation error
      page.name = this.newName;
      _$modalInstance.close(page);
    }
  }

  angular
    .module('bonitasoft.designer.editor.menu-bar')
    .controller('SaveAsPopUpController', SaveAsPopUpController);
})();
