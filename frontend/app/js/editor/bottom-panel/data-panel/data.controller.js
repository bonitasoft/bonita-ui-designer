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

  $scope.delete = dataName => delete $scope.page.data[dataName];

  $scope.save = function(data) {
    $scope.page.data[data.$$name] = {
      exposed: data.exposed,
      type: data.type,
      value:  $scope.isExposed(data) ? '' : data.value,
    };
  };

  $scope.getType = data => ($scope.isExposed(data)) ? '(' + gettextCatalog.getString('Exposed') + ')' : $scope.getLabel(data.type);

  $scope.isExposed = data => $scope.exposableData && data.exposed;

  $scope.openDataPopup = function(key) {
    var modalInstance = $uibModal.open({
      templateUrl: 'js/editor/bottom-panel/data-panel/data-popup.html',
      controller: 'DataPopupController',
      resolve: {
        mode: () => mode,
        pageData: () => artifact.data,
        data: () => key && angular.extend({}, artifact.data[key], { $$name: key })
      }
    });

    modalInstance.result.then($scope.save);

  };

  $scope.openHelp = () => $uibModal.open({ templateUrl: 'js/editor/bottom-panel/data-panel/help-popup.html', size: 'lg' });

  $scope.getVariables = (serchTerm) => {
    function toMatchSearchTerm(variable) {
      function contains(value, search) {
        return angular.lowercase(value || '').indexOf(angular.lowercase(search) || '') !== -1;
      }

      return contains(variable.name, serchTerm) || contains(variable.value, serchTerm);
    }

    return Object.keys($scope.page.data)
      .map((name) => {
        var variable = $scope.page.data[name];
        return Object.defineProperty(variable, 'name', { enumerable: false, value: name });
      })
      .filter(toMatchSearchTerm);
  };

  $scope.sort = (sortCriteria) => {
    $scope.isReversedSorting = $scope.sortCriteria === sortCriteria ?  !$scope.isReversedSorting : false;
    $scope.sortCriteria = sortCriteria;
  };

  $scope.sort('name'); // default sort is name
});
