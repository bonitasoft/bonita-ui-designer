(function() {

  'use strict';

  class MigrationReportPopUpController {
    constructor($uibModalInstance, artifact, migrationReport) {
      this.$uibModalInstance = $uibModalInstance;
      this.migrationReport = migrationReport;
      this.artifact = artifact;
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('MigrationReportPopUpController', MigrationReportPopUpController);
})();
