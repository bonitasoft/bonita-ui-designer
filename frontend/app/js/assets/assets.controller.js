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
    .module('bonitasoft.designer.assets')
    .controller('AssetCtrl', AssetCtrl);

  function AssetCtrl($uibModal, artifact, artifactRepo, mode, assetsService) {

    var vm = this;
    vm.component = artifact;
    vm.component.assets = vm.component.assets || [];
    vm.filters = assetsService.getFilters();
    vm.isExternal = assetsService.isExternal;
    vm.isPageAsset = assetsService.isPageAsset;
    vm.deactivateAsset = deactivateAsset;
    vm.incrementOrderAsset = incrementOrderAsset;
    vm.decrementOrderAsset = decrementOrderAsset;
    vm.delete = deleteAsset;
    vm.openAssetPreviewPopup = openAssetPreviewPopup;
    vm.getAssetUrl = getAssetUrl;
    vm.openAssetPopup = openAddUpdateAssetPopup;
    vm.openHelp = openHelp;

    function incrementOrderAsset(asset) {
      return artifactRepo.incrementOrderAsset(vm.component.id, asset).then(refreshComponentAssets);
    }

    function decrementOrderAsset(asset) {
      return artifactRepo.decrementOrderAsset(vm.component.id, asset).then(refreshComponentAssets);
    }

    function refreshComponentAssets() {
      artifactRepo.loadAssets(vm.component).then(function(response) {
        vm.component.assets = response;
      });
    }

    function deactivateAsset(asset) {
      return artifactRepo.desactivateAsset(vm.component.id, asset).then(updateInactiveAssetsList);
    }

    function updateInactiveAssetsList() {
      var inactiveAssets = vm.component.assets.filter(function(asset) {
        return !asset.active;
      }).map(function(asset) {
        return asset.id;
      });
      vm.component.inactiveAssets = (inactiveAssets.length) ? inactiveAssets : undefined;
    }

    function deleteAsset(asset) {
      artifactRepo.deleteAsset(vm.component.id, asset).then(function() {
        vm.component.assets = vm.component.assets.filter(function(actual) {
          return actual.id !== asset.id;
        });
      });
    }

    function openAssetPreviewPopup(asset) {
      $uibModal.open({
        templateUrl: 'js/assets/asset-preview-popup.html',
        controller: 'AssetPreviewPopupCtrl',
        resolve: {
          asset: () => asset,
          component: () => vm.component,
          mode: () => mode
        }
      });
    }

    function getAssetUrl(asset) {
      return assetsService.getAssetUrl(asset, mode, vm.component);
    }

    function openAddUpdateAssetPopup(asset) {
      var modalInstance = $uibModal.open({
        templateUrl: 'js/assets/asset-popup.html',
        controller: 'AssetPopupCtrl',
        controllerAs: 'vm',
        resolve: {
          asset: () =>  asset,
          assets: () => vm.component.assets,
          mode: () => mode,
          artifact: () => artifact,
          artifactRepo: () => artifactRepo
        }
      });
      modalInstance.result.then(updateList);
    }

    function updateList(asset) {
      var replaced = false;
      vm.component.assets = vm.component.assets.map(function(item) {
        if (item.id === asset.id) {
          replaced = true;
          return asset;
        }
        return item;
      });
      if (!replaced) {
        vm.component.assets.push(asset);
      }
    }

    function openHelp(elm) {
      $uibModal.open({
        templateUrl: 'js/assets/help-popup.html',
        size: 'lg',
        controller: function($scope) {
          'ngInject';
          $scope.isPage = (elm !== 'widget');
        }
      });
    }

  }

})();
