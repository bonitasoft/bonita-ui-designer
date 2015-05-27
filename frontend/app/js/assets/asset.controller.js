(function () {

  angular.module('pb.assets').controller('AssetCtrl', function ($scope, artifact, artifactRepo, mode, assets) {

    'use strict';

    $scope.searchedAsset = assets.initFilterMap();
    $scope.isAssetPage = mode==='page';

    if(artifactRepo.loadAssets){
      artifactRepo.loadAssets(artifact.id).then(function (response) {
        $scope.assets = response.data;
      });
    }
    else{
      $scope.assets = artifact.assets;
    }

    $scope.filterBySearchedAsset = function (asset) {
      var assetType =  $scope.searchedAsset.filter(function(elt){
        return elt.key === asset.type;
      })[0];
      return assetType ? assetType.filter : false;
    };

  });
})
();
