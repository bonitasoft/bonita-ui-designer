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

  $scope.propertyValue = $scope.propertyValue || {
      type: $scope.property.bond,
      value: $scope.property.defaultValue
    };

  $scope.isDisplayed = function () {

    // If there is no expression we will always display the option
    if (!$scope.property.showFor) {
      return true;
    }
    return $scope.$eval($scope.property.showFor);
  };

  $scope.getDataNames = function () {
    return Object.keys($scope.pageData);
  };

  this.toggleExpressionEditor = function () {
    $scope.propertyValue.type = $scope.propertyValue.type === 'expression' ? 'constant': 'expression';
  };

  this.isExpression = function () {
    return $scope.propertyValue.type === 'expression';
  };

  this.getBindingPlaceholder = function (property) {
    return property.type === 'boolean' ? 'variableName === true' : 'variableName';
  };

  // should be shared with widget editor
  var supportedTypes = ['boolean', 'choice', 'collection', 'float', 'html', 'integer'];

  this.getFieldTemplate = function (property) {
    var type = supportedTypes.indexOf(property.type) >= 0 ? property.type : 'text';
    return 'js/editor/properties-panel/field/' + type + '.html';
  };

  this.getBondTemplate = function (property) {
    return 'js/editor/properties-panel/bond/' + property.bond + '.html';
  };
});
