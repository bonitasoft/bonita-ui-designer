(function () {

  angular.module('pb.assets').controller('AssetPopupCtrl', function ($scope, $modalInstance, alerts, assetsService, asset, mode, artifact) {

    'use strict';

    var urlPrefixForLocalAsset = 'rest/' + mode + 's/' + artifact.id + '/assets/';

    $scope.asset = asset;
    $scope.isNewAsset = asset === undefined;

    //All datas (type, sources) are defined in the assets service.
    $scope.assetTypes = assetsService.getTypes();
    $scope.assetSources = assetsService.getSources();

    //Asset is converted in another object for the html form
    $scope.newAsset = assetsService.assetToForm(asset);

    $scope.cancel = cancel;
    $scope.isExternalAsset = isExternalAsset;
    $scope.onSuccess = onSuccess;
    $scope.onError = onError;
    $scope.saveExternalAsset = saveExternalAsset;
    $scope.updateSavingAction = updateSavingAction;

    function isExternalAsset(asset) {
      return asset.source === assetsService.getExternalSource();
    }

    /**
     * An external asset is saved by a $http call
     */
    function saveExternalAsset(data) {
      data.isNew = $scope.isNewAsset;
      $modalInstance.close(data);
    }

    /**
     * A local asset (file) is saved by the submit of the html form
     */
    function onSuccess(response) {
      //Even if a problem occurs in the backend a response is sent with a message
      //If the message has a type and a message this an error
      if (response && response.type && response.message) {
        alerts.addError(response);
      }
      $modalInstance.close();
    }

    /**
     * If an error occured on saving, the message is displayed in alerts area
     */
    function onError(error) {
      alerts.addError(error);
      $modalInstance.dismiss('cancel');
    }

    /**
     * User clicked on Cancel button
     */
    function cancel() {
      $modalInstance.dismiss();
    }

    /**
     * The form action target is not the same according to the asset type : css, js or img
     */
    function updateSavingAction(type){
      $scope.assetSavingAction = urlPrefixForLocalAsset + type;
    }

  });

})();
