(function () {

  angular.module('pb.assets').controller('AssetPreviewPopupCtrl', function ($scope, $modalInstance, asset, url) {

    'use strict';

    $scope.url = url;
    $scope.asset = asset;

    $scope.cancel = function () {
      $modalInstance.dismiss();
    };

  });

})();
