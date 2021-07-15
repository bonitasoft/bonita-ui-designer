(function() {

  'use strict';

  class MigrationReportPopUpController {
    constructor($scope, $uibModalInstance, keyBindingService, artifact, migrationReport) {
      this.$uibModalInstance = $uibModalInstance;
      this.migrationReport = migrationReport;
      this.artifact = artifact;

      // Pause keyBindingService to avoid move selection in background when user press arrow key
      keyBindingService.pause();
      // Unpause keyBindingService when popup is destroy
      $scope.$on('$destroy', function() {
        keyBindingService.unpause();
      });
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('MigrationReportPopUpController', MigrationReportPopUpController);
})();
