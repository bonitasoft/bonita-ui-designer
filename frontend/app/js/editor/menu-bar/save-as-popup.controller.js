(function () {

  'use strict';

  angular
    .module('bonitasoft.designer.editor.menu-bar')
    .controller('SaveAsPopUpController', SaveAsPopUpController);

  function SaveAsPopUpController($scope, $modalInstance, page) {
    $scope.page = page;
    $scope.newName = page.name;

    $scope.ok = function() {
      $scope.page.name = $scope.newName;
      $modalInstance.close($scope.page);
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  }

})();
