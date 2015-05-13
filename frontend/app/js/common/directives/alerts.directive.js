/**
 * Element directive used to display alerts of the alerts service.
 * Usage:
 *     <alerts></alerts>
 */
angular.module('pb.directives').directive('alerts', function() {

  'use strict';

  return {
    restrict: 'E',
    controller: function($scope, alerts) {
      $scope.alerts = alerts.alerts;

      $scope.remove = function(index) {
        alerts.remove(index);
      };

    },
    template: '<div class="alerts-wrapper" ng-show="alerts.length > 0"><alert ng-repeat="alert in alerts" type="{{ alert.type }}" close="remove($index)" class="text-center">{{ alert.message }}</alert></div>'
  };
});
