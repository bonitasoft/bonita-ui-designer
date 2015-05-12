/**
 * Controller of the componentMover directive
 */
angular.module('pb.directives').controller('ComponentMoverDirectiveCtrl', function($scope, arrays) {
  var componentRow = function() {
    return $scope.component.$$parentContainerRow.row;
  };

  function hasParent() {
    return  $scope.component.hasOwnProperty('$$parentContainerRow');
  }

  $scope.moveLeftVisible = function() {
    return  hasParent() && arrays.moveLeftPossible($scope.component, componentRow());
  };

  $scope.moveRightVisible = function() {
    return hasParent() && arrays.moveRightPossible($scope.component, componentRow());
  };

  $scope.moveLeft = function() {
    arrays.moveLeft($scope.component, componentRow());
  };

  $scope.moveRight = function() {
    arrays.moveRight($scope.component, componentRow());
  };
});
