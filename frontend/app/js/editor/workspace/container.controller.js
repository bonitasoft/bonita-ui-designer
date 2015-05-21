/**
 * Controller of the container directive
 */
angular.module('pb.directives').controller('ContainerDirectiveCtrl', function ($scope, $rootScope, arrays, componentUtils) {

  'use strict';

  $scope.resizable = true;

  $scope.moveRowUp = function (row, event) {
    arrays.moveLeft(row, $scope.container.rows);
    event.stopPropagation();
  };

  $scope.moveRowDown = function (row, event) {
    arrays.moveRight(row, $scope.container.rows);
    event.stopPropagation();
  };

  $scope.moveRowUpVisible = function (row) {
    return arrays.moveLeftPossible(row, $scope.container.rows);
  };

  $scope.moveRowDownVisible = function (row) {
    return arrays.moveRightPossible(row, $scope.container.rows);
  };

  /**
   * When I drop an item already configured I want to remove it from its current row in order to attach it in another one.
   * @param  {Object} data Widget configuration
   * @param  {Array} row  current row
   * @return {void}
   */
  $scope.dropItem = function(data, row) {

    // You cannot drop a container inside itself, nor in its children
    if(!componentUtils.isMovable(data, $scope.component || $scope.container)) {
      return;
    }

    $scope.editor.selectRow($scope.container, row);
    $scope.editor.removeCurrentComponent(data);
  };

  $scope.removeRow = function (row) {
    $scope.editor.selectRow($scope.container, row);
    $scope.editor.removeCurrentRow();
  };

  $scope.dropAtEndOfTheRow = function (data, event, row) {

    row = row || [];
    // If you are trying to dragAndDrop a widget already defined
    if(data.$$widget) {

      if(!componentUtils.isMovable(data, $scope.component || $scope.container))  {
        return;
      }
      $scope.editor.selectRow($scope.container, row);
      $scope.editor.dropElement(data);
      return;
    }

    $scope.editor.addComponentToRow(data, $scope.container, row);
  };

  $scope.dropBeforeRow = function (data, event, rowIndex, rows) {
    var currentComponent = $scope.component || $scope.container;
    // Do not add a row if the container is not movable
    if(currentComponent.id || componentUtils.isMovable(data, currentComponent)) {
      rows.splice(rowIndex, 0, []);
    }
    $scope.dropAtEndOfTheRow(data, event, rows[rowIndex]);
  };

  $scope.dropAfterRow = function (data, event, rowIndex, rows) {
    var currentComponent = $scope.component || $scope.container;

    // Do not add a row if the container is not movable
    if(currentComponent.id || componentUtils.isMovable(data, currentComponent)) {
      rowIndex = rowIndex + 1;
      rows.splice(rowIndex, 0, []);
    }
    $scope.dropAtEndOfTheRow(data, event, rows[rowIndex]);
  };

  $scope.isEmpty = function(container) {
    return componentUtils.isEmpty(container);
  };
});
