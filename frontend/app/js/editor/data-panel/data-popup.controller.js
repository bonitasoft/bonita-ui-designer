angular.module('pb.controllers')
  .controller('DataPopupController', function ($scope, dataTypeService, $modalInstance, mode, pageData, data) {

    'use strict';

    $scope.dataTypes = dataTypeService.getDataTypes();
    $scope.getLabel = dataTypeService.getDataLabel;

    $scope.pageData = pageData;
    $scope.isNewData = data === undefined;
    $scope.newData = data || dataTypeService.createData();
    $scope.exposableData = mode !== 'page';

    $scope.isDataNameUnique = function (dataName) {
      return !dataName || !pageData[dataName];
    };

    $scope.updateValue = function (dataType) {
      $scope.newData.value = dataTypeService.getDataDefaultValue(dataType);
    };

    $scope.save = function (data) {
      $modalInstance.close(data);
    };

    $scope.cancel = function () {
      $modalInstance.dismiss();
    };
  });
