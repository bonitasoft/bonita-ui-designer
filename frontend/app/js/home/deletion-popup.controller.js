angular.module('pb.home').controller('DeletionPopUpController', function($scope, $modalInstance, artifact, type) {

  'use strict';

  /**
   * artifact is the element to be deleted. Could be a page or a widget. Should have an id
   */
  $scope.artifact = artifact;
  $scope.artifact.type = type;

  $scope.ok = function() {
    $modalInstance.close($scope.artifact.id);
  };

  $scope.cancel = function() {
    $modalInstance.dismiss('cancel');
  };

});
