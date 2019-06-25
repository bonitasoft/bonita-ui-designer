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

  function AssetCtrl($scope, $uibModal, artifact, artifactRepo, assetRepo, mode, assetsService, assetEditPopup) {

    var vm = this;
    var assetListByType = [];
    vm.types = assetsService.getFiltersTypes();
    vm.scopeFilter = assetsService.getScopes();
    vm.getAssetsByTypeForCurrentScope = getAssetsByTypeForCurrentScope;
    vm.getNbrOfAssetsByType = getNbrOfAssetsByType;
    vm.isEditable = isEditable;
    vm.isExternal = assetsService.isExternal;
    vm.isPageAsset = assetsService.isPageAsset;
    vm.component = artifact;
    vm.component.assets = vm.component.assets || [];
    vm.deactivateAsset = deactivateAsset;
    vm.incrementOrderAsset = incrementOrderAsset;
    vm.decrementOrderAsset = decrementOrderAsset;
    vm.delete = deleteAsset;
    vm.openAssetPreviewPopup = openAssetPreviewPopup;
    vm.getAssetUrl = getAssetUrl;
    vm.openAssetPopup = openAddUpdateAssetPopup;
    vm.openAssetEditPopup = openAssetEditPopup;
    vm.openHelp = openHelp;
    vm.isViewable = isViewable;
    vm.assetAlreadyOnMove = false;
    vm.isAssetMatchSearchTerm = isAssetMatchSearchTerm;

    function isAssetMatchSearchTerm(asset) {
      return angular.lowercase(asset.name || '').indexOf(angular.lowercase($scope.searchTerm) || '') !== -1;
    }

    function getAssetsByTypeForCurrentScope(type) {
      if (!assetListByType[type]) {
        assetListByType[type] = [];
      }
      assetListByType[type].splice(0);

      let allAssets = Array.concat(vm.component.assets, assetsService.getBaseFrameworkAsset());

      var filterResult = allAssets.filter(function(asset) {
        const scope = vm.scopeFilter[asset.scope];
        return asset.type === type && scope && scope.filter && vm.isAssetMatchSearchTerm(asset);
      });

      filterResult.forEach(function(asset) {
        assetListByType[type].push(asset);
      });
      return assetListByType[type];
    }

    function getNbrOfAssetsByType(type) {
      let allAssets = Array.concat(vm.component.assets, assetsService.getBaseFrameworkAsset());

      var nbrOfAssets = allAssets.filter(function(asset) {
        return asset.type === type;
      });
      return nbrOfAssets.length;
    }

    function incrementOrderAsset(asset) {
      if (vm.assetAlreadyOnMove) {
        return;
      }
      return artifactRepo.incrementOrderAsset(vm.component.id, asset).then(refreshComponentAssets.bind(null, asset));
    }

    function decrementOrderAsset(asset) {
      if (vm.assetAlreadyOnMove) {
        return;
      }
      return artifactRepo.decrementOrderAsset(vm.component.id, asset).then(refreshComponentAssets.bind(null, asset));
    }

    function refreshComponentAssets(asset) {
      vm.assetAlreadyOnMove = true;
      let memoryAssets = angular.copy(vm.component.assets);
      artifactRepo.loadAssets(vm.component).then(function(response) {
        response.forEach(asset => pushToArray(memoryAssets, asset));
        vm.component.assets = memoryAssets;
      });
      if (mode === 'widget') {
        vm.assetAlreadyOnMove = false;
        return;
      }
      // Timeout is needeed to wait table refresh
      setTimeout(() => addClassShaker(asset), 50);
    }

    function addClassShaker(asset) {
      let element = document.getElementById(asset.id);
      element.classList.add('moving-asset');
      // Remove class move-asset to allow to move another asset time
      setTimeout(() => {
        element.classList.remove('moving-asset');
        vm.assetAlreadyOnMove = false;
      }, 1000);
    }

    function pushToArray(arr, obj) {
      const index = arr.findIndex((e) => {
        if (e.id && obj.id) {
          return e.id === obj.id;
        } else {
          return e.name === obj.name;
        }
      });

      if (index === -1) {
        arr.push(obj);
      } else {
        arr[index] = obj;
      }
    }

    function deactivateAsset(asset) {
      return artifactRepo.desactivateAsset(vm.component.id, asset).then(updateInactiveAssetsList);
    }

    function updateInactiveAssetsList() {
      var inactiveAssets = vm.component.assets.filter(function(asset) {
        return !asset.active;
      }).map(function(asset) {
        return asset.id || asset.name;
      });
      vm.component.inactiveAssets = (inactiveAssets.length) ? inactiveAssets : undefined;
    }

    function deleteAsset(asset) {
      let modalInstance = $uibModal.open({
        templateUrl: 'js/confirm-delete/confirm-delete-popup.html',
        controller: 'ConfirmDeletePopupController',
        controllerAs: 'ctrl',
        size: 'md',
        resolve: {
          artifact: () => asset.name,
          type: () => 'asset'
        }
      });

      modalInstance.result.then(
        () =>
          assetRepo.deleteAsset(vm.component.id, asset).then(function() {
            vm.component.assets = vm.component.assets.filter(function(actual) {
              return actual.id !== asset.id;
            });
          })
      );
    }

    function openAssetPreviewPopup(asset) {
      $uibModal.open({
        templateUrl: 'js/assets/asset-preview-popup.html',
        controller: 'AssetPreviewPopupCtrl',
        resolve: {
          asset: () => asset,
          component: () => vm.component
        },
        size: 'lg'
      });
    }

    function openAssetEditPopup(asset) {
      if (assetsService.isExternal(asset)) {
        openAddUpdateAssetPopup(asset);
      } else {
        openLocalAssetEditPopup(asset);
      }
    }

    function openLocalAssetEditPopup(asset) {
      assetEditPopup.open({
          asset,
          assetRepo,
          component: vm.component
        }
      );
    }

    function getAssetUrl(asset) {
      return assetsService.getAssetUrl(asset, vm.component);
    }

    function openAddUpdateAssetPopup(asset) {
      var modalInstance = $uibModal.open({
        templateUrl: 'js/assets/asset-popup.html',
        controller: 'AssetPopupCtrl',
        controllerAs: 'vm',
        resolve: {
          asset: () => asset,
          assets: () => vm.component.assets,
          mode: () => mode,
          artifact: () => artifact,
          assetRepo: () => assetRepo,
          scope: () =>  mode
        }
      });
      modalInstance.result.then(updateList);
    }

    function updateList(asset) {
      var replaced = false;
      asset.scope = mode;
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

    function isEditable(asset) {
      if (!asset.scope) {
        return asset.type !== 'img';
      }
      return asset.type !== 'img' && (asset.scope && asset.scope === mode);
    }

    function isViewable(asset) {
      return !asset.external && !isEditable(asset);
    }
  }

})();
