/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
  'use strict';

  class ImportArtifactCtrl {
    constructor($uibModal, $scope, importArtifactService) {
      this.$uibModal = $uibModal;
      this.$scope = $scope;
      this.importArtifactService = importArtifactService;

      this.type = 'artifact';
      this.url = 'import/artifact';
      this.filename = '';
      this.popupTitle = 'Import a UI Designer artifact';
    }

    close() {
      this.isOpen = false;
      this.filename = '';
    }

    onComplete(response) {
      var importPromise = this.importArtifactService
        .manageImportResponse(this.type, true, response);

      importPromise.finally(() => this.close());
      importPromise.then((importReport) => (!!importReport) && this.manageImportReport(importReport))
        .then(this.$scope.refreshAll);
    }

    manageImportReport(importReport) {
      return this.$uibModal.open({
        templateUrl: 'js/home/import/import-report-popup.html',
        controller: 'ImportReportPopupController',
        controllerAs: 'importReport',
        resolve: {
          importReport: () => importReport
        }
      }).result;
    }
  }

  angular
    .module('bonitasoft.designer.home')
    .directive('uidImportArtifact', () => ({
      scope: true,
      templateUrl: 'js/home/import/home-import.html',
      controller: ImportArtifactCtrl,
      controllerAs: 'import'
    }));

})();
