angular.module('pb.custom-widget').controller('PropertyEditorPopupCtrl', function($scope, param, $modalInstance, $timeout) {

  'use strict';

  $scope.paramToUpdate = param;

  /**
   * All types available for the properties
   * @type {Array}
   */
  $scope.types = ['text', 'choice', 'html', 'integer', 'boolean', 'collection'];

  // default type is text
  $scope.currentParam = $scope.paramToUpdate ? angular.copy(param) : {type: 'text'};

  $scope.ok = function() {
    $modalInstance.close({param: $scope.currentParam, paramToUpdate: $scope.paramToUpdate});
  };

  $scope.cancel = function() {
    $modalInstance.dismiss('cancel');
  };

  // Delay autofocus once modal is fully appeared
  // FIX IE bug on focus position
  $scope.animationFinished = false;
  $timeout(function() {
    $scope.animationFinished = true;
  }, 300);

});
