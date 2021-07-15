(function() {

  'use strict';

  class ConvertPopUpController {
    constructor($scope, $uibModalInstance, keyBindingService, page, gettext) {
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
      // Pause keyBindingService to avoid move selection in background when user press arrow key
      keyBindingService.pause();
      // Unpause keyBindingService when popup is destroy
      $scope.$on('$destroy', function() {
        keyBindingService.unpause();
      });
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
