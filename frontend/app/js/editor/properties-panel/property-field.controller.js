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

  $scope.displayCondition = function () {

    // If there is no expression we will always display the option
    if (!$scope.property.showFor) {
      return true;
    }

    return $scope.$eval($scope.property.showFor);
  };

  $scope.getDataNames = function () {
    return Object.keys($scope.pageData);
  };

  $scope.shouldBeLinked = function () {
    return $scope.propertyValue && $scope.propertyValue.type === 'data';
  };

  $scope.unlink = function () {
    if ($scope.property.bidirectional) {
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

  if ($scope.property.bidirectional) {
    $scope.propertyValue.type = 'data';
    $scope.linked = true;
  }

  this.getBindingPlaceholder = function (property) {
    return property.type === 'boolean' ? 'variableName === true' : 'variableName';
  };
});
