(function () {
  'use strict';

  angular.module('pb.generator.directives')
    .directive('pbPropertyValues', function ($parse, bindingsFactory, propertyValuesFactory, bindingContextFactory) {

      function pbPropertyValuesCompile(tElement, tAttributes) {
        return {
          pre: function ($scope, elem, attr, pbModelCtrl) {
            var pbPropertyValues = propertyValuesFactory.get(tAttributes.pbPropertyValues);
            bindingsFactory.create(
              pbPropertyValues,
              bindingContextFactory.create(pbModelCtrl, $scope),
              $scope.properties = {});
            $scope.properties.isBound = function (property) {
              return !!pbPropertyValues[property] &&
                pbPropertyValues[property].type === 'data' &&
                !!pbPropertyValues[property].value;
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
