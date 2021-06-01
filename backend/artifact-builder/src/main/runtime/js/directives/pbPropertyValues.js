(function() {
  'use strict';

  angular.module('bonitasoft.ui.directives')
    .directive('pbPropertyValues', function($parse, $q, $timeout, $log, bindingsFactory, propertyValuesFactory, bindingContextFactory) {

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
