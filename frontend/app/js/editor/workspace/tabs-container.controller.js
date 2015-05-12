/**
 * Controller of the tabsContainer directive
 */
angular.module('pb.directives').controller('TabsContainerDirectiveCtrl', function($scope, arrays) {

  'use strict';

  $scope.openTab = function(tab, event) {
    $scope.tabsContainer.$$openedTab = tab;
    $scope.editor.selectTab(tab, event);
  };

  $scope.isOpened = function(tab) {
    return $scope.tabsContainer.$$openedTab === tab;
  };

  $scope.tabsContainer.$$openedTab = $scope.tabsContainer.tabs[0];

  $scope.moveTabLeftVisible = function(tab) {
    return $scope.editor.isCurrentTab(tab) && arrays.moveLeftPossible(tab, $scope.tabsContainer.tabs);
  };

  $scope.moveTabRightVisible = function(tab) {
    return $scope.editor.isCurrentTab(tab) && arrays.moveRightPossible(tab, $scope.tabsContainer.tabs);
  };

  $scope.moveTabLeft = function(tab) {
    arrays.moveLeft(tab, $scope.tabsContainer.tabs);
  };

  $scope.moveTabRight = function(tab) {
    arrays.moveRight(tab, $scope.tabsContainer.tabs);
  };
});
