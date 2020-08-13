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

  angular
    .module('bonitasoft.designer.common.services')
    .factory('migration', migrationService);

  function migrationService($q, $uibModal, gettextCatalog, alerts, $localStorage) {

    class Migration {
      constructor() {
        this.$uibModal = $uibModal;
        this.alerts = alerts;
        this.$localStorage = $localStorage;
        this.lastReport = {};
      }

      handleMigrationStatus(id, status) {
        this.lastReport = {};
        if (!status.compatible) {
          let error = {};
          error.message = gettextCatalog.getString(`${id} is not compatible with this UI Designer version. A newer version is required.`);
          throw(error);
        } else if (status.migration) {
          let storage = this.$localStorage.bonitaUIDesigner;
          if (!storage || !storage.doNotShowMigrationMessageAgain) {
            return this.migrationConfirm(id);
          }
        }
        return $q.resolve();
      }

      migrationConfirm(artifactId) {
        let modalInstance = $uibModal.open({
          templateUrl: 'js/confirm-popup/confirm-migrate-popup.html',
          controller: 'ConfirmPopupNotAgainController',
          controllerAs: 'ctrl',
          size: 'md',
          resolve: {
            artifact: () => artifactId,
            type: () => 'artifact',
            action: () => 'migrate'
          }
        });
        return modalInstance.result;
      }

      handleMigrationNotif(id, migrationReport) {
        this.lastReport = migrationReport;
        let status = migrationReport.status;
        let successMessage = gettextCatalog.getString(`${id} has been migrated successfully. Check migration details to more details.`);
        switch (status) {
          case 'success': {
            alerts.addInfo(successMessage, 5000);
            break;
          }
          case 'error': {
            let error = {};
            error.message = gettextCatalog.getString(`Error during migration of ${id}. Check migration details to more details.`);
            throw(error);
          }
          default: {
            break;
          }
        }
      }

      getLastReport() {
        return this.lastReport;
      }
    }

    return new Migration();
  }

})();
