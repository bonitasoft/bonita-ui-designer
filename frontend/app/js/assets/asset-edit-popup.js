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
(() => {
  'use strict';

  class AssetEditPopupCtrl {

    constructor(asset, component, aceMode, $uibModalInstance, assetRepo, assetErrorManagement, $scope, keyBindingService) {
      this.asset = asset;
      this.component = component;
      this.$uibModalInstance = $uibModalInstance;
      this.assetErrorManagement = assetErrorManagement;
      this.assetRepo = assetRepo;
      this.aceMode = aceMode;
      this.content = this.initialContent = '';
      this.scope = $scope;

      this.assetRepo
        .loadLocalAssetContent(component.id, asset)
        .then((response) => this.content = this.initialContent = response.data);

      keyBindingService.bind(['ctrl+s', 'command+s'], () => {
        $scope.$apply(() => this.save());
        // prevent default browser action
        return false;
      });
    }

    save() {
      return this.assetRepo
        .updateLocalAssetContent(this.component.id, this.asset, this.content)
        .then((response) => this.assetErrorManagement.manageErrorsFromResponse(response))
        .then(() => this.initialContent = this.content)
        .then(() => this.scope.$broadcast('saved'));
    }

    saveAndClose() {
      this.save().then(() => this.$uibModalInstance.close());
    }

    hasChanged() {
      return this.content !== this.initialContent;
    }
  }

  class AssetEditPopUp {
    constructor($uibModal, assetsService) {
      this.$uibModal = $uibModal;
      this.assetsService = assetsService;
    }

    open({ asset, component, assetRepo }) {
      return this.$uibModal.open({
        templateUrl: 'js/assets/asset-edit-popup.html',
        controller: AssetEditPopupCtrl,
        controllerAs: 'vm',
        resolve: {
          asset: () => asset,
          component: () => component,
          assetRepo: () => assetRepo,
          aceMode: () => this.assetsService.getType(asset.type).aceMode
        },
        size: 'xxl'
      });
    }
  }

  angular
    .module('bonitasoft.designer.assets')
    .controller('AssetEditPopupCtrl', AssetEditPopupCtrl)
    .service('assetEditPopup', AssetEditPopUp);

})();
