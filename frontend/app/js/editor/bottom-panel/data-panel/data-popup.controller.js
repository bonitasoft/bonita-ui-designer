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
  .controller('DataPopupController', function($scope, dataTypeService, $uibModalInstance, mode, pageData, data, apiExamples, dataManagementRepo, gettextCatalog, businessDataUpdateService) {

    'use strict';
    const BUSINESS_DATA_TYPE = 'businessdata';

    $scope.apiExamples = apiExamples.get();
    $scope.example = $scope.apiExamples[0];
    $scope.examplesCollapsed = true;
    $scope.advancedExamplesCollapsed = true;
    $scope.dataTypes = dataTypeService.getDataTypes();
    $scope.getLabel = dataTypeService.getDataLabel;
    $scope.pageData = pageData;
    $scope.isNewData = data === undefined;
    $scope.editBusinessDataQueries = true;
    $scope.newData = data || dataTypeService.save();
    $scope.exposableData = mode !== 'page';
    $scope.offlineMode = false;
    $scope.businessDataRepoIsEmpty = false;
    $scope.validity = false;

    dataManagementRepo.getDataObjects().then(data => {
      $scope.offlineMode = data.error;
      if ($scope.newData.type === BUSINESS_DATA_TYPE) {
        editBusinessDataVariable(data);
      } else {
        $scope.businessDataRepoIsEmpty = data.objects.length === 0;
        $scope.editBusinessDataQueries = data.objects.length > 0;
        $scope.businessObjects = data.objects;
      }
    });

    function editBusinessDataVariable(data) {
      let loadData = JSON.parse($scope.newData.displayValue);
      // Check if any business object is define in data repository
      if (data.objects.length === 0) {
        $scope.businessObjects = [{
          name: loadData.businessObjectName,
          id: loadData.id
        }];
        $scope.businessDataRepoIsEmpty = true;
        $scope.editBusinessDataQueries = false;
      } else {
        $scope.businessObjects = data.objects;
        // Find business Object
        let selectBO = data.objects.filter(bo => loadData.id === bo.id);
        if (selectBO.length === 0) {
          $scope.editBusinessDataQueries = false;
          $scope.businessObjects.push({
            name: loadData.businessObjectName,
            id: loadData.id,
            description: null
          });
        }
      }

      if ($scope.newData.type === BUSINESS_DATA_TYPE) {
        $scope.updateBusinessObjectValue(loadData);
      }
    }

    $scope.businessDataDisplayWarningError = function() {
      return !$scope.editBusinessDataQueries || $scope.offlineMode || $scope.businessDataRepoIsEmpty;
    };

    $scope.isDataNameUnique = function(dataName) {
      return !dataName || !pageData[dataName];
    };

    $scope.updateValue = function(dataType) {
      $scope.newData.displayValue = dataTypeService.getDataDefaultValue(dataType);
    };

    $scope.save = function(dataToSave) {
      if ($scope.newData.type === BUSINESS_DATA_TYPE) {
        $scope.removeListeners();
      }
      $uibModalInstance.close(dataToSave);
    };

    $scope.updateBusinessObjectValue = function(businessObject) {
      $scope.newData.businessObject = $scope.businessObjects.filter(bo => bo.id === businessObject.id)[0];
      $scope.newData.variableInfo = businessObject || {};
      $scope.newData.queries = dataManagementRepo.getQueries(businessObject.id);
      $scope.newData.lang = gettextCatalog.getCurrentLanguage();

      $scope.businessDataUpdate = businessDataUpdateService.create(businessObject, $scope.variableInfo);

      $scope.handleQueryChanged = (e) =>  {
        $scope.newData.variableInfo = $scope.businessDataUpdate.queryChanged(e);
        $scope.validity = $scope.businessDataUpdate.isDataValid(e);
        $scope.$apply();
      };
      document.addEventListener('queryChanged', $scope.handleQueryChanged);
      $scope.removeListeners = () => {
        document.removeEventListener('queryChanged', $scope.handleQueryChanged);
      };

    };

    $scope.canBeSaved = function() {
      if ($scope.newData.type === BUSINESS_DATA_TYPE) {
        return $scope.validity;
      } else {
        return $scope.addData.$valid;
      }
    };

    $scope.cancel = function() {
      if ($scope.newData.type === BUSINESS_DATA_TYPE) {
        $scope.removeListeners();
      }
      $uibModalInstance.dismiss();
    };

  });
