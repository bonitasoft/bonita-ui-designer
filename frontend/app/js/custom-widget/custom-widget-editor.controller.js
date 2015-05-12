angular.module('pb.custom-widget').controller('CustomWidgetEditorCtrl', function($scope, widget, widgetRepo, alerts, $modal, $window, keymaster, gettextCatalog) {

  'use strict';

  $scope.widget = widget;

  var saveSuccessMsg = gettextCatalog.getString('Custom widget [ {{name}} ] successfully saved', { name: $scope.widget.name });

  keymaster('ctrl+s', function() {
    $scope.$apply(function() {
      $scope.save();
    });
    // prevent default browser action
    return false;
  });

  /**
   * Updates the property
   * @param paramName the name of the property to update
   * @param param - the property to update
   */
  $scope.updateParam = function(paramName, param) {
    widgetRepo.updateProperty($scope.widget.id, paramName, param).then(function(response) {
      $scope.widget.properties = response.data;
    });
  };

  /**
   * Adds a new property
   * @param param - the param to add
   */
  $scope.addParam = function(param) {
    widgetRepo.addProperty($scope.widget.id, param).then(function(response) {
      $scope.widget.properties = response.data;
    });
  };

  /**
   * Deletes an existing property, by dropping it from the collection
   * @param paramIndex - the index of the property to drop
   */
  $scope.deleteParam = function(param) {
    widgetRepo.deleteProperty($scope.widget.id, param.name).then(function(response) {
      $scope.widget.properties = response.data;
    });
  };

  /**
   * Saves a widget and gives it an id based on its name (Awesome Widget -> awesome-widget)
   */
  $scope.save = function() {
    widgetRepo.save($scope.widget).then(function() {
      alerts.addSuccess(saveSuccessMsg, 2000);
    });

  };

  $scope.saveAndExport = function() {
    widgetRepo.save($scope.widget).then(function() {
      $window.location = widgetRepo.exportUrl($scope.widget);
    });
  };

  $scope.createOrUpdate = function(param) {

    var modalInstance = $modal.open({
      templateUrl: 'createProperty.html',
      backdrop: 'static',
      controller: 'PropertyEditorPopupCtrl',
      resolve: {
        param: function() {
          return param;
        }
      }
    });

    modalInstance.result
      .then(function(result) {
        if (result.paramToUpdate) {
          $scope.updateParam(result.paramToUpdate.name, result.param);
        } else {
          $scope.addParam(result.param);
        }
      });
  };
});
