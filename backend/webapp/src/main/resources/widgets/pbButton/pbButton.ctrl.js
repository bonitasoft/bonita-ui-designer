function PbButtonCtrl($scope, $http, $timeout, $location, $log, $window) {

  'use strict';

  this.action = function action() {
    if ($scope.properties.action === 'Remove from collection') {
      removeFromCollection();
    } else if ($scope.properties.action === 'Add to collection') {
      addToCollection();
    } else if ($scope.properties.action === "Start process") {
      var id = getUrlParam('id');
      if (id) {
        doRequestDelayed('POST', '/bonita/API/bpm/process/' + id + '/instantiation', getUserParam());
      } else {
        $log.log('Impossible to retrieve the process definition id value from the URL');
      }
    } else if ($scope.properties.action === 'Submit task') {
      var id = getUrlParam('id');
      if (id) {
        doRequestDelayed('POST', '/bonita/API/bpm/userTask/' + getUrlParam('id') + '/execution', getUserParam());
      } else {
        $log.log('Impossible to retrieve the task id value from the URL');
      }
    } else if ($scope.properties.url) {
      doRequestDelayed($scope.properties.action, $scope.properties.url);
    }
  };

  function removeFromCollection() {
    if ($scope.properties.collectionToModify) {
      if (!Array.isArray($scope.properties.collectionToModify)) {
        throw 'Collection property for widget button should be an array, but was ' + $scope.properties.collectionToModify;
      }

      if ($scope.properties.collectionPosition === 'First') {
        $scope.properties.collectionToModify.shift();
      } else {
        $scope.properties.collectionToModify.pop();
      }
    }
  }

  function addToCollection() {
    if (!$scope.properties.collectionToModify) {
      $scope.properties.collectionToModify = [];
    }
    if (!Array.isArray($scope.properties.collectionToModify)) {
      throw 'Collection property for widget button should be an array, but was ' + $scope.properties.collectionToModify;
    }
    var item = angular.copy($scope.properties.valueToAdd);

    if ($scope.properties.collectionPosition === 'First') {
      $scope.properties.collectionToModify.unshift(item);
    } else {
      $scope.properties.collectionToModify.push(item);
    }
  }

  // we delayed the doRequest to ensure dataToSend is updated
  // this usefull when copy() update the dataToSend object.
  function doRequestDelayed(method, url, params) {
    $timeout(function () {
      doRequest(method, url, params);
    }, false);
  }

  /**
   * Execute a get/post request to an URL
   * It also bind custom data from success|error to a data
   * @return {void}
   */
  function doRequest(method, url, params) {
    var req = {
      method: method,
      url: url,
      data: angular.copy($scope.properties.dataToSend),
      params: params
    };

    $http(req)
      .success(function (data) {
        if ($scope.properties.targetUrlOnSuccess) {
          $window.top.location.assign($scope.properties.targetUrlOnSuccess);
        }
        $scope.properties.dataFromSuccess = data;
      })
      .error(function (data) {
        $scope.properties.dataFromError = data;
      });
  }

  function getUserParam() {
    var userId = getUrlParam('user');
    if (userId) {
      return {'user': userId};
    }
    return {};
  }

  /**
   * Extract the param value from a URL query
   * e.g. if param = "id", it extracts the id value in the following cases:
   *  1. http://localhost/bonita/portal/resource/process/ProcName/1.0/content/?id=8880000
   *  2. http://localhost/bonita/portal/resource/process/ProcName/1.0/content/?param=value&id=8880000&locale=en
   *  3. http://localhost/bonita/portal/resource/process/ProcName/1.0/content/?param=value&id=8880000&locale=en#hash=value
   * @returns {id}
   */
  function getUrlParam(param) {
    var paramValue = $location.absUrl().match('[//?&]' + param + '=([^&#]*)($|[&#])');
    if (paramValue) {
      return paramValue[1];
    }
    return '';
  }
}
