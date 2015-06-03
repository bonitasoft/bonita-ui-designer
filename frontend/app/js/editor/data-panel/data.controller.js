angular.module('pb.controllers').controller('DataCtrl', function($scope, dataTypeService, $location, $modal, artifactRepo, artifact, mode) {

  'use strict';

  $scope.searchedData = '';
  $scope.page = artifact;
  $scope.pageData = artifact.data;
  $scope.getLabel = dataTypeService.getDataLabel;
  $scope.exposableData = mode !== 'page';

  function updateData(data) {
    $scope.page.data = data;
    $scope.filterPageData();
  }

  $scope.delete = function(dataName) {
    artifactRepo.deleteData($scope.page, dataName)
      .then(function(response) {
        updateData(response.data);
      });
  };

  $scope.save = function(data) {
    artifactRepo.saveData($scope.page, data)
      .then(function(response) {
        updateData(response.data);
      });
  };

  $scope.filterPageData = function () {
    function matchData(pattern, key, data ) {
      return key.indexOf(pattern.trim()) !== -1 || angular.toJson(data || {}).indexOf(pattern.trim()) !== -1 ;
    }

    $scope.pageData = Object.keys($scope.page.data).reduce(function(data, key){
      if (matchData($scope.searchedData, key, artifact.data[key].value)) {
        data[key] = artifact.data[key];
      }
      return data;
    }, {});
  };

  $scope.clearFilter = function() {
    $scope.searchedData = '';
    $scope.filterPageData();
  };

  $scope.clearFilterVisible = function(search) {
    return (search||'').trim().length > 0;
  };

  $scope.openDataPopup = function(key) {
    var modalInstance = $modal.open({
      templateUrl: 'js/editor/data-panel/data-popup.html',
      backdrop: 'static',
      controller: 'DataPopupController',
      resolve: {
        mode: function() {
          return mode;
        },
        pageData: function() {
          return artifact.data;
        },
        data: function() {
          return key && angular.extend({}, artifact.data[key], {$$name: key});
        }
      }
    });

    modalInstance.result.then($scope.save);

  };
});
