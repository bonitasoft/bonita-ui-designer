(function () {

  angular.module('pb.assets').controller('AssetCtrl', function ($scope, artifact, artifactRepo, mode, assets) {

    'use strict';

    $scope.component = artifact;

    $scope.searchedAsset = assets.initFilterMap();
    $scope.isAssetPage = mode==='page';

    $scope.filterBySearchedAsset = function (asset) {
      var assetType =  $scope.searchedAsset.filter(function(elt){
        return elt.key === asset.type;
      })[0];
      return assetType ? assetType.filter : false;
    };

  });
})
();
