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
angular.module('bonitasoft.designer.custom-widget').value('BONDS', {
  'variable': {
    type: 'text',
    name: 'Bidirectional bond',
    template: 'js/editor/help/bidirectional-bond-help.html'
  },
  'expression': {
    name: 'Dynamic value',
    template: 'js/editor/help/dynamic-bond-help.html'
  },
  'interpolation': {
    type: 'text',
    name: 'Interpolation',
    template: 'js/editor/help/interpolation-bond-help.html'
  },
  'constant': {
    name: 'Constant',
    template: 'js/editor/help/constant-bond-help.html'
  }
})
  .controller('PropertyEditorPopupCtrl', function($scope, param, $modalInstance, BONDS) {

    'use strict';

    $scope.paramToUpdate = param;

    /**
     * All types available for the properties
     * @type {Array}
     */
    $scope.types = ['text', 'choice', 'html', 'integer', 'boolean', 'collection'];
    /**
     * All bonds available for the properties
     * @type {Array}
     */
    $scope.selectedBond = ($scope.paramToUpdate && $scope.paramToUpdate.bond) || 'expression';
    $scope.bonds = BONDS;

    $scope.isTypeChoicable = function() {
      return $scope.currentParam.type === 'choice' && ($scope.selectedBond === 'constant' || $scope.selectedBond === 'expression');
    };

    $scope.isTypeSelectable = function() {
      return $scope.selectedBond === 'constant' || $scope.selectedBond === 'expression';
    };

    // default type is text
    $scope.currentParam = $scope.paramToUpdate ? angular.copy(param) : {
      type: 'text'
    };

    $scope.updateType = function() {
      if ($scope.selectedBond && $scope.bonds[$scope.selectedBond] && $scope.bonds[$scope.selectedBond].type) {
        $scope.currentParam.type = $scope.bonds[$scope.selectedBond].type;
      }
      $scope.currentParam.bond = $scope.selectedBond;
    };

    $scope.ok = function() {
      if ($scope.selectedBond === 'variable') {
        $scope.currentParam.defaultValue = null;
      }
      $modalInstance.close({
        param: $scope.currentParam,
        paramToUpdate: $scope.paramToUpdate
      });
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  });
