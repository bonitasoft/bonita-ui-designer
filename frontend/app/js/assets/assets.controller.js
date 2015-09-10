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
(function () {

  'use strict';

  angular
    .module('bonitasoft.designer.assets')
    .controller('AssetCtrl', AssetCtrl);

  function AssetCtrl($modal, artifact, artifactRepo, mode, assetsService) {

    var vm = this;
    vm.component = artifact;
    vm.filters = assetsService.getFilters();
    vm.isExternal = assetsService.isExternal;
    vm.isPageAsset = assetsService.isPageAsset;
    vm.desactivateAsset = desactivateAsset;
    vm.incrementOrderAsset = incrementOrderAsset;
    vm.decrementOrderAsset = decrementOrderAsset;
    vm.delete = deleteAsset;
    vm.openAssetPreviewPopup = openAssetPreviewPopup;
    vm.openAssetPopup = openAddUpdateAssetPopup;
    vm.openHelp = openHelp;

    //Load assets
    refresh();

    function incrementOrderAsset(asset) {
      return artifactRepo.incrementOrderAsset(vm.component.id, asset).then(refresh);
    }

    function decrementOrderAsset(asset) {
      return artifactRepo.decrementOrderAsset(vm.component.id, asset).then(refresh);
    }

    function desactivateAsset(asset) {
      return artifactRepo.desactivateAsset(vm.component.id, asset).then(refresh);
    }

    function deleteAsset(asset) {
      artifactRepo.deleteAsset(vm.component.id, asset).then(refresh);
    }

    function openAssetPreviewPopup(asset) {
      $modal.open({
        templateUrl: 'js/assets/asset-preview-popup.html',
        controller: 'AssetPreviewPopupCtrl',
        resolve: {
          asset: function () {
            return asset;
          },
          component: function () {
            return vm.component;
          },
          mode: function() {
            return mode;
          }
        }
      });
    }

    function openAddUpdateAssetPopup(asset) {
      var modalInstance = $modal.open({
        templateUrl: 'js/assets/asset-popup.html',
        controller: 'AssetPopupCtrl',
        controllerAs: 'vm',
        resolve: {
          asset: function () {
            return asset;
          },
          mode: function () {
            return mode;
          },
          artifact: function () {
            return artifact;
          },
          artifactRepo: function() {
            return artifactRepo;
          }
        }
      });
      modalInstance.result.then(refresh);
    }

    /**
     * Refresh assets in scope
     */
    function refresh() {
      artifactRepo.loadAssets(vm.component)
        .then(function (response) {
          vm.assets = response;
          vm.component.assets = response.filter(function (asset) {
            //In the page editor, we filter on the assets linked to the page
            return asset.scope !== 'WIDGET';
          });
          var inactiveAssets = response.filter(function (asset) {
            return !asset.active;
          }).map(function (asset) {
            return asset.id;
          });
          vm.component.inactiveAssets = (inactiveAssets.length) ? inactiveAssets : undefined;
        }
      );
    }

    function openHelp(elm) {
      $modal.open({
        templateUrl: 'js/assets/help-popup.html',
        size: 'lg',
        controller: function ($scope, $modalInstance) {
          $scope.isPage = (elm !== 'widget');
          $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
          };
        }
      });
    }

  }

})();
