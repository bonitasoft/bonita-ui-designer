/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Controller of the container directive
 */
angular.module('bonitasoft.designer.editor.whiteboard').controller('ContainerDirectiveCtrl', function($scope, $rootScope, arrays, componentUtils) {

  'use strict';

  $scope.resizable = true;

  $scope.moveRowUp = function(row, event) {
    arrays.moveLeft(row, $scope.container.rows);
    event.stopPropagation();
  };

  $scope.moveRowDown = function(row, event) {
    arrays.moveRight(row, $scope.container.rows);
    event.stopPropagation();
  };

  $scope.moveRowUpVisible = function(row) {
    return arrays.moveLeftPossible(row, $scope.container.rows);
  };

  $scope.moveRowDownVisible = function(row) {
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
    if (!componentUtils.isMovable(data, $scope.component || $scope.container)) {
      return;
    }

    $scope.editor.selectRow($scope.container, row);
    $scope.editor.removeCurrentComponent(data, row);
  };

  $scope.removeRow = function(row) {
    $scope.editor.selectRow($scope.container, row);
    $scope.editor.removeCurrentRow();
  };

  $scope.dropAtEndOfTheRow = function(data, event, row) {

    row = row || [];
    // If you are trying to dragAndDrop a widget already defined
    if (data.$$widget) {

      if (!componentUtils.isMovable(data, $scope.component || $scope.container))  {
        return;
      }
      $scope.editor.selectRow($scope.container, row);
      $scope.editor.dropElement(data);
      return;
    }

    $scope.editor.addComponentToRow(data, $scope.container, row);
  };

  $scope.dropBeforeRow = function(data, event, rowIndex, rows) {
    var currentComponent = $scope.component || $scope.container;
    // Do not add a row if the container is not movable
    if (currentComponent.id || componentUtils.isMovable(data, currentComponent)) {
      rows.splice(rowIndex, 0, []);
    }
    $scope.dropAtEndOfTheRow(data, event, rows[rowIndex]);
  };

  $scope.dropAfterRow = function(data, event, rowIndex, rows) {
    var currentComponent = $scope.component || $scope.container;

    // Do not add a row if the container is not movable
    if (currentComponent.id || componentUtils.isMovable(data, currentComponent)) {
      rowIndex = rowIndex + 1;
      rows.splice(rowIndex, 0, []);
    }
    $scope.dropAtEndOfTheRow(data, event, rows[rowIndex]);
  };

  $scope.isEmpty = function(container) {
    return componentUtils.isEmpty(container);
  };

  $scope.isRepeated = function(container) {
    return container && container.propertyValues && container.propertyValues.repeatedCollection && container.propertyValues.repeatedCollection.value;
  };
});
