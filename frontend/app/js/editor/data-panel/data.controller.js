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
angular.module('bonitasoft.designer.editor.data-panel').controller('DataCtrl', function($scope, dataTypeService, $location, $uibModal, artifact, mode) {

  'use strict';

  $scope.searchedData = '';
  $scope.page = artifact;
  $scope.pageData = artifact.data;
  $scope.getLabel = dataTypeService.getDataLabel;
  $scope.exposableData = mode !== 'page';
  $scope.keys = Object.keys;

  $scope.delete = function(dataName) {
    delete $scope.page.data[dataName];
    $scope.filterPageData();
  };

  $scope.save = function(data) {
    $scope.page.data[data.$$name] = {
      exposed: data.exposed,
      type: data.type,
      value: data.value
    };
    $scope.filterPageData();
  };

  $scope.filterPageData = function() {
    function matchData(pattern, key, data) {
      return key.indexOf(pattern.trim()) !== -1 || angular.toJson(data || {}).indexOf(pattern.trim()) !== -1;
    }

    $scope.pageData = Object.keys($scope.page.data).reduce(function(data, key) {
      if (matchData($scope.searchedData, key, artifact.data[key].value)) {
        data[key] = artifact.data[key];
      }
      return data;
    }, {});
  };

  $scope.$watch('searchedData', () => {
    $scope.filterPageData();
  });

  $scope.openDataPopup = function(key) {
    var modalInstance = $uibModal.open({
      templateUrl: 'js/editor/data-panel/data-popup.html',
      controller: 'DataPopupController',
      resolve: {
        mode: () => mode,
        pageData: () => artifact.data,
        data: () => key && angular.extend({}, artifact.data[key], { $$name: key })
      }
    });

    modalInstance.result.then($scope.save);

  };

  $scope.openHelp = () => $uibModal.open({ templateUrl: 'js/editor/data-panel/help-popup.html', size: 'lg' });
});
