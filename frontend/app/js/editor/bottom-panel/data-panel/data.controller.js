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
angular.module('bonitasoft.designer.editor.bottom-panel.data-panel').controller('DataCtrl', function($scope, dataTypeService, $location, $uibModal, artifact, mode, gettextCatalog) {

  'use strict';

  $scope.searchedData = '';
  $scope.page = artifact;
  $scope.getLabel = dataTypeService.getDataLabel;
  $scope.exposableData = mode !== 'page';

  $scope.delete = function(dataName) {
    let modalInstance = $uibModal.open({
      templateUrl: 'js/confirm-delete/confirm-delete-popup.html',
      controller: 'ConfirmDeletePopupController',
      controllerAs: 'ctrl',
      size: 'md',
      resolve: {
        artifact: () => dataName,
        type: () => 'variable'
      }
    });

    modalInstance.result.then(
      () => delete $scope.page.variables[dataName]
    );
  };

  $scope.save = function(data) {
    $scope.page.variables[data.$$name] = {
      exposed: data.exposed,
      type: data.type,
      displayValue: $scope.isExposed(data) ? '' : (data.type === 'businessdata' ? JSON.stringify(data.variableInfo.data) : data.displayValue)
    };
  };

  $scope.getType = data => ($scope.isExposed(data)) ? '(' + gettextCatalog.getString('Exposed') + ')' : $scope.getLabel(data.type);

  $scope.isExposed = data => $scope.exposableData && data.exposed;

  $scope.openDataPopup = function(variable) {
    let varName = variable ? variable.name : undefined;
    var modalInstance = $uibModal.open({
      templateUrl: 'js/editor/bottom-panel/data-panel/data-popup.html',
      controller: 'DataPopupController',
      resolve: {
        mode: () => mode,
        pageData: () => artifact.variables,
        data: () => varName && angular.extend({}, artifact.variables[varName], { $$name: varName })
      }
    });

    modalInstance.result.then($scope.save);

  };

  $scope.openHelp = () => $uibModal.open({
    templateUrl: 'js/editor/bottom-panel/data-panel/help-popup.html',
    size: 'lg'
  });

  $scope.getVariables = (serchTerm) => {
    function toMatchSearchTerm(variable) {
      function contains(value, search) {
        return angular.lowercase(value || '').indexOf(angular.lowercase(search) || '') !== -1;
      }

      return contains(variable.name, serchTerm) || contains(variable.displayValue, serchTerm);
    }

    return Object.keys($scope.page.variables)
      .map((name) => {
        var variable = $scope.page.variables[name];
        return Object.defineProperty(variable, 'name', { enumerable: false, value: name });
      })
      .filter(toMatchSearchTerm);
  };

  $scope.displayValue = (data) => {
    if (data.type === 'businessdata') {
      let businessData = JSON.parse(data.displayValue);
      return businessData.displayValue;
    }
    return data.displayValue;
  };

  $scope.sort = (sortCriteria) => {
    $scope.isReversedSorting = $scope.sortCriteria === sortCriteria ? !$scope.isReversedSorting : false;
    $scope.sortCriteria = sortCriteria;
  };

  $scope.sort('name'); // default sort is name
});
