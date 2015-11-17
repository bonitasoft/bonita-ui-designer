(function() {
  'use strict';

  angular.module('bonitasoft.ui.directives')
    .directive('pbPropertyValues', function($parse, $q, $timeout, $log, bindingsFactory, propertyValuesFactory, bindingContextFactory, pendingStatus) {

      function pbPropertyValuesCompile(tElement, tAttributes) {
        return {
          pre: function($scope, elem, attr, pbModelCtrl) {
            var pbPropertyValues = propertyValuesFactory.get(tAttributes.pbPropertyValues);
            bindingsFactory.create(
              pbPropertyValues,
              bindingContextFactory.create(pbModelCtrl, $scope),
              $scope.properties = {});

            $scope.properties.isBound = function(propertyName) {
              return !!pbPropertyValues[propertyName] &&
                pbPropertyValues[propertyName].type === 'variable' &&
                !!pbPropertyValues[propertyName].value;
            };

            $scope.properties.waitFor = function(propertyName) {
              // This comment is mandatory to force variable resolution
              $log.log('resolving property ', propertyName,' Current value: ', $scope.properties[propertyName]);

              var defer = $q.defer();
              var removeFn = pendingStatus.listen(function() {
                defer.resolve();
              });

              return defer.promise.then(removeFn);
            };
          }
        };
      }

      return {
        restrict: 'A',
        scope: true,
        require: '^pbModel',
        compile: pbPropertyValuesCompile
      };
    });
})();
