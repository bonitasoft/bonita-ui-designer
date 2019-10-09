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
angular.module('bonitasoft.designer.editor.bottom-panel.data-panel')
  .controller('DataPopupController', function($scope, dataTypeService, $uibModalInstance, mode, pageData, data, apiExamples, dataManagementRepo) {

    'use strict';

    $scope.apiExamples = apiExamples.get();
    $scope.example = $scope.apiExamples[0];
    $scope.examplesCollapsed = true;
    $scope.advancedExamplesCollapsed = true;
    $scope.dataTypes = dataTypeService.getDataTypes();
    $scope.getLabel = dataTypeService.getDataLabel;
    $scope.pageData = pageData;
    $scope.isNewData = data === undefined;
    $scope.newData = data || dataTypeService.save();
    $scope.exposableData = mode !== 'page';

    dataManagementRepo.getDataObjects().then(data => {
      $scope.businessObjects = data;

      if ($scope.newData.type === 'businessdata') {
        let loadData = JSON.parse($scope.newData.displayValue);
        $scope.updateBusinessObjectValue(loadData);
      }
    });

    $scope.isDataNameUnique = function(dataName) {
      return !dataName || !pageData[dataName];
    };

    $scope.updateValue = function(dataType) {
      $scope.newData.displayValue = dataTypeService.getDataDefaultValue(dataType);
    };

    $scope.save = function(dataToSave) {
      $uibModalInstance.close(dataToSave);
    };

    $scope.updateBusinessObjectValue = function(businessObject) {
      $scope.newData.businessObject = $scope.businessObjects.filter(bo => bo.id === businessObject.id)[0];
      $scope.newData.variableInfo = businessObject || {};
      dataManagementRepo.getQueries(businessObject.id).then(res => {
        $scope.newData.queries = res;
      });
    };

    $scope.cancel = function() {
      $uibModalInstance.dismiss();
    };
  });
