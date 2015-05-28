(function () {

  angular.module('pb.assets').controller('AssetCtrl', function ($scope, $modal, artifact, artifactRepo, mode, assets) {

    'use strict';

    var component = artifact;
    $scope.searchedAsset = assets.initFilterMap();
    $scope.isAssetPage = mode==='page';

    artifactRepo.loadAssets(artifact).then(function (response) {
        $scope.assets = response.data;
    });

    $scope.filterBySearchedAsset = function (asset) {
      var assetType =  $scope.searchedAsset.filter(function(elt){
        return elt.key === asset.type;
      })[0];
      return assetType ? assetType.filter : false;
    };

    $scope.openAssetPreviewPopup = function(element) {
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

  });
})
();
