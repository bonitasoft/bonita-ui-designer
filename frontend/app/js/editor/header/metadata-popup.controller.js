(function() {

  'use strict';

  class MetadataPopUpController {
    constructor($uibModalInstance, page, resources, gettext) {
      'ngInject';
      this.$uibModalInstance = $uibModalInstance;
      this.page = page;
      this.displayName = page.displayName;
      this.description = page.description;
      this.resources = resources;
      this.types = {
        page: {
          key: 'page',
          value: gettext('Application page')
        },
        form: {
          key: 'form',
          value: gettext('Process form')
        }
      };
      if (this.types[page.type]) {
        this.type = this.types[page.type];
      }
    }

    save(form) {
      if (!form.$invalid) {
        this.page.displayName = this.displayName;
        this.page.description = this.description;
        if (this.type) {
          this.page.type = this.type.key;
        }
        this.$uibModalInstance.close();
      }
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('MetadataPopUpController', MetadataPopUpController);
})();
