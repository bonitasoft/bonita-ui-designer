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
  class ConfirmDeletePopupController {
    constructor($scope, $uibModalInstance, keyBindingService, artifact, type) {
      this.$scope = $scope;
      this.$uibModalInstance = $uibModalInstance;
      this.artifact = artifact;
      this.type = type;

      // Pause keyBindingService to avoid move selection in background when user press arrow key
      keyBindingService.pause();
      // Unpause keyBindingService when popup is destroy
      $scope.$on('$destroy', function() {
        keyBindingService.unpause();
      });
    }

    confirm() {
      this.$uibModalInstance.close(this.artifact);
    }

    cancel() {
      this.$uibModalInstance.dismiss('cancel');
    }

  }

  angular.module('bonitasoft.designer.confirm-delete').controller('ConfirmDeletePopupController', ConfirmDeletePopupController);
})();
