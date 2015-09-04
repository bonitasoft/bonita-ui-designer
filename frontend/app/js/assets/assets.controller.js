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

  angular.module('bonitasoft.designer.assets').controller('AssetCtrl', function ($scope, $modal, $q, artifact, artifactRepo, mode, assetsService) {

    'use strict';

    var ctrl = this;
    var component = artifact;
    $scope.filters = createFilters();
    $scope.isExternal = assetsService.isExternal;
    $scope.isPageAsset = assetsService.isPageAsset;
    $scope.desactivateAsset = desactivateAsset;
    $scope.incrementOrderAsset = incrementOrderAsset;
    $scope.decrementOrderAsset = decrementOrderAsset;

    function incrementOrderAsset(asset) {
      return artifactRepo.incrementOrderAsset(component.id, asset).then(refresh);
    }

    function decrementOrderAsset(asset) {
      return artifactRepo.decrementOrderAsset(component.id, asset).then(refresh);
    }

    function desactivateAsset(asset) {
      return artifactRepo.desactivateAsset(component.id, asset).then(refresh);
    }

    //Load assets
    refresh();

    /**
     * Delete an asset
     */
    $scope.delete = function (asset) {
      artifactRepo.deleteAsset(component.id, asset).then(refresh);
    };

    /**
     * Popup to see the content of the asset file
     */
    $scope.openAssetPreviewPopup = function (element) {
      var asset = element;

      $modal.open({
        templateUrl: 'js/assets/asset-preview-popup.html',
        controller: 'AssetPreviewPopupCtrl',
        resolve: {
          asset: function () {
            return asset;
          },
          url: function () {
            //Url depends on the nature of component
            //In custom widget editor, component is a widget
            if (mode === 'widget') {
              return 'preview/widget/' + component.id + '/assets/' + asset.type + '/' + asset.name + '?format=text';
            }
            //In page editor widget id is stored in asset.componentId if the asset scope is WIDGET
            else if (asset.scope === 'WIDGET') {
              return 'preview/widget/' + asset.componentId + '/assets/' + asset.type + '/' + asset.name + '?format=text';
            }
            //The last case is to see a page asset
            return 'preview/page/' + component.id + '/assets/' + asset.type + '/' + asset.name + '?format=text';
          }
        }
      });
    };

    /**
     * Popup to add or update assets
     */
    $scope.openAssetPopup = function (element) {
      var asset = element;

      var modalInstance = $modal.open({
        templateUrl: 'js/assets/asset-popup.html',
        controller: 'AssetPopupCtrl',
        resolve: {
          asset: function () {
            return asset;
          },
          mode: function () {
            return mode;
          },
          artifact: function () {
            return artifact;
          }
        }
      });
      //Action launched after a local asset upload or an external asset creation
      modalInstance.result.then(ctrl.createOrUpdate).then(refresh);
    };

    /**
     * Refresh assets in scope
     */
    function refresh() {
      artifactRepo.loadAssets(component)
        .then(function (response) {
          $scope.assets = response;
          component.assets = response.filter(function (asset) {
            //In the page editor, we filter on the assets linked to the page
            return asset.scope !== 'WIDGET';
          });
          var inactiveAssets = response.filter(function (asset) {
            return !asset.active;
          }).map(function (asset) {
            return asset.id;
          });
          component.inactiveAssets = (inactiveAssets.length) ? inactiveAssets : undefined;
        }
      );
    }

    $scope.openHelp = function (elm) {
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
    };

    /**
     * Create or update an asset
     */
    this.createOrUpdate = function (data) {
      if (data) {
        return artifactRepo.createAsset(component.id, assetsService.formToAsset(data));
      }
      else {
        //A local asset is created via a form send in the popup. We just have to return a promise
        return $q.when({});
      }
    };

    function createFilters() {
      return assetsService.getTypes()
        .map(function transformToFilter(type) {
          return {
            key: type.key,
            value: {
              label: type.value,
              value: type.filter
            }
          };
        })
        .reduce(function createObject(filters, filter) {
          filters[filter.key] = filter.value;
          return filters;
        }, {});
    }
  });
})
();
