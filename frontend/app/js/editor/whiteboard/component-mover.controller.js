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
 * Controller of the componentMover directive
 */
angular.module('bonitasoft.designer.editor.whiteboard').controller('ComponentMoverDirectiveCtrl', function($scope, arrays) {
  var componentRow = function() {
    return $scope.component.$$parentContainerRow.row;
  };

  function hasParent() {
    return $scope.component && $scope.component.hasOwnProperty('$$parentContainerRow');
  }

  $scope.moveLeftVisible = function() {
    return hasParent() && arrays.moveLeftPossible($scope.component, componentRow());
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
