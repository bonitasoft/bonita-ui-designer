(function() {

  'use strict';

  class MetadataPopUpController {
    constructor($scope, $uibModalInstance, keyBindingService, page, resources) {
      'ngInject';
      this.$uibModalInstance = $uibModalInstance;
      this.page = page;
      this.displayName = page.displayName;
      this.description = page.description;
      this.resources = resources;

      // Pause keyBindingService to avoid move selection in background when user press arrow key
      keyBindingService.pause();
      // Unpause keyBindingService when popup is destroy
      $scope.$on('$destroy', function() {
        keyBindingService.unpause();
      });
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
