/**
 * Controller of the componentMover directive
 */
angular.module('pb.directives').controller('PropertyFieldDirectiveCtrl', function ($scope) {

  'use strict';

  $scope.linked = false;

  // initialize propertyValue if not yet initialized. Could appear when creating custom widgets and adding properties
  // when custom widget is already in the page
  if (!$scope.propertyValue) {
    $scope.propertyValue = {
      type: 'constant',
      value: $scope.property.defaultValue
    };
  }

  $scope.displayCondition = function() {

    // If there is no expression we will always display the option
    if(!$scope.property.showFor) {
      return true;
    }

    return $scope.$eval($scope.property.showFor);
  };

  $scope.getDataNames = function() {
    return Object.keys($scope.pageData);
  };

  $scope.shouldBeLinked = function() {
    return $scope.propertyValue && $scope.propertyValue.type === 'data';
  };

  $scope.unlink = function () {
    if($scope.property.bidirectional) {
      return;
    }

    $scope.oldLinkedValue = angular.copy($scope.propertyValue.value);
    $scope.propertyValue.value = $scope.oldUnlikedValue;
    $scope.propertyValue.type = 'constant';
    $scope.linked = false;
  };

  $scope.link = function () {
    $scope.oldUnlikedValue = angular.copy($scope.propertyValue.value);
    $scope.propertyValue.value = $scope.oldLinkedValue;
    $scope.propertyValue.type = 'data';
    $scope.linked = true;
  };

  if($scope.property.bidirectional) {
    $scope.propertyValue.type = 'data';
    $scope.linked = true;
  }
});
