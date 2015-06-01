(function () {

  angular.module('pb.assets').controller('AssetCtrl', function ($scope, $modal, artifact, artifactRepo, mode, assets) {

    'use strict';

    var component = artifact;
    $scope.searchedAsset = assets.initFilterMap();
    $scope.isPageAsset = mode==='page';
    $scope.isExternal = assets.isExternal;

    //Load assets
    refresh();

    /**
     * Refresh assets in scope
     */
    function refreshAssetsInScope(response){
      $scope.assets = response;
      $scope[mode].assets = artifact;
    }
    function refresh(){
      artifactRepo.loadAssets(artifact).then(refreshAssetsInScope);
    }

    /**
     * Create an asset via popup
     */
    function createAndRefresh(data) {
      if(data){
        artifactRepo
          .createAsset(artifact.id, assets.formToAsset(data))
          .then(artifactRepo.loadAssets.bind(null, component))
          .then(refreshAssetsInScope);
      }
      else{
        refresh();
      }
    }

    /**
     * Use for asset table filtering
     */
    $scope.filterBySearchedAsset = function (asset) {
      var assetType = $scope.searchedAsset.filter(function (elt) {
        return elt.key === asset.type;
      })[0];
      return assetType ? assetType.filter : false;
    };

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
        backdrop: 'static',
        controller: 'AssetPreviewPopupCtrl',
        resolve: {
          asset: function () {
            return asset;
          },
          url: function () {
            //Url depends on the nature of component
            //In custom widget editor, component is a widget
            if(mode==='widget'){
              return 'preview/widget/' + component.id + '/assets/' + asset.type + '/' + asset.name;
            }
            //In page editor widget id is stored in asset.componentId if the asset scope is WIDGET
            else if(asset.scope==='WIDGET'){
              return 'preview/widget/' + asset.componentId + '/assets/' + asset.type + '/' + asset.name;
            }
            //The last case is to see a page asset
            return 'preview/page/' + component.id + '/assets/' + asset.type + '/' + asset.name;
          }
        }
      });
    };

    /**
     * Popup to add or update assets
     */
    $scope.openAssetPopup = function(element) {
      var asset = element;

      var modalInstance = $modal.open({
        templateUrl: 'js/assets/asset-popup.html',
        backdrop: 'static',
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
      modalInstance.result.then(createAndRefresh);
    };
  });

})
();
