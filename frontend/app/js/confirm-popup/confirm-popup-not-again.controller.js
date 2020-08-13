/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
  class ConfirmPopupNotAgainController {
    constructor($scope, $uibModalInstance, $localStorage, artifact, type, action) {
      this.$scope = $scope;
      this.$uibModalInstance = $uibModalInstance;
      this.$localStorage = $localStorage;
      this.artifact = artifact;
      this.type = type;
      this.action = action;
      this.doNotShowAgain = false;
    }

    confirm() {
      if (!this.$localStorage.bonitaUIDesigner) {
        this.$localStorage.bonitaUIDesigner = {};
      }
      if (this.action === 'migrate') {
        this.$localStorage.bonitaUIDesigner.doNotShowMigrationMessageAgain = this.doNotShowAgain;
      }
      this.$uibModalInstance.close(this.artifact);
    }

    cancel() {
      this.$uibModalInstance.dismiss('cancel');
    }

  }

  angular.module('bonitasoft.designer.confirm-popup').controller('ConfirmPopupNotAgainController', ConfirmPopupNotAgainController);
})();
