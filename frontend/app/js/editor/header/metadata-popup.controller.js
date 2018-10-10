(function() {

  'use strict';

  class MetadataPopUpController {
    constructor($uibModalInstance, page, resources) {
      'ngInject';
      this.$uibModalInstance = $uibModalInstance;
      this.page = page;
      this.displayName = page.displayName;
      this.description = page.description;
      this.resources = resources;
    }

    save(form) {
      if (!form.$invalid) {
        this.page.displayName = this.displayName;
        this.page.description = this.description;
        this.$uibModalInstance.close();
      }
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('MetadataPopUpController', MetadataPopUpController);
})();
