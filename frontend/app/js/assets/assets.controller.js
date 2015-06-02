(function () {

  angular.module('pb.assets').controller('AssetCtrl', function ($scope, $modal, $q, artifact, artifactRepo, mode, assetsService) {

    'use strict';

    var component = artifact;
    $scope.searchedAsset = assetsService.initFilterMap();
    $scope.isExternal = assetsService.isExternal;

    //Load assets
    refresh();

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
      //Action launched after a local asset upload or an external asset creation
      modalInstance.result.then(this.createOrUpdate).then(refresh);
    };

    /**
     * Refresh assets in scope
     */
    function refresh(){
      artifactRepo.loadAssets(component)
        .then(function(response){
          $scope.assets = response;
          $scope[mode].assets = response.filter(function(asset){
            //In the page editor, we filter on the assets linked to the page
            return asset.scope!=='WIDGET';
          });
        }
      );
    }

    /**
     * Create or update an asset
     */
    this.createOrUpdate = function (data){
      if(data){
        if(data.isNew){
          //An external asset is created by a POST request. Specific data are not send to backend. formToAsset does the transformation
          return artifactRepo.createAsset(component.id, assetsService.formToAsset(data));
        }
        else{
          //If data exist, we delete it (the user can change the name or the type). The first step is the deletion of the old asset
          //and the second the creation of the new one
          var oldAsset = assetsService.formToAsset(data);
          oldAsset.type = data.oldtype;
          oldAsset.name = data.oldname;
          return artifactRepo.deleteAsset(component.id, oldAsset).then(artifactRepo.createAsset.bind(null, component.id, assetsService.formToAsset(data)));
        }
      }
      else{
        //A local asset is created via a form send in the popup. We just have to return a promise
        return $q.when({});
      }
    };
  });

})
();
