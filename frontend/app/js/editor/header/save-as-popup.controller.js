(function() {

  'use strict';

  class SaveAsPopUpController {
    constructor($scope, $uibModalInstance, keyBindingService, page, gettext) {
      this.$uibModalInstance = $uibModalInstance;
      this.page = page;
      this.newName = page.name;
      this.types = {
        page: {
          key: 'page',
          value: gettext('Application page'),
          tooltip: gettext('Linked to an application descriptor.')
        },
        form: {
          key: 'form',
          value: gettext('Process form'),
          tooltip: gettext('Linked to a process instantiation or a task, using the contract')
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

    ok() {
      let page = angular.copy(this.page);     // copy page to avoid side effects in case of creation error
      page.name = this.newName;
      if (this.type) {
        page.type = this.type.key;
      }
      this.$uibModalInstance.close(page);
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('SaveAsPopUpController', SaveAsPopUpController);
})();
