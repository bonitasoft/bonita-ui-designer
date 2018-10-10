(function() {

  'use strict';

  class ConvertPopUpController {
    constructor($uibModalInstance, page, gettext) {
      'ngInject';
      this.$uibModalInstance = $uibModalInstance;
      this.page = page;
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

    save() {
      if (this.type) {
        this.page.type = this.type.key;
      }
      this.$uibModalInstance.close();
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('ConvertPopUpController', ConvertPopUpController);
})();
