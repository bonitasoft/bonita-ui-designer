(function () {
  'use strict';

  angular.module('pb.generator.directives')
    .directive('pbPropertyValues', function ($parse, bindingsFactory, propertyValuesFactory) {

      function pbPropertyValuesCompile(tElement, tAttributes) {
        return {
          pre: function ($scope, elem, attr, pbModelCtrl) {
            var context = pbModelCtrl.createGateway();
            Object.defineProperty(context, '$item', {
              get: function () {
                return $scope.$item;
              },
              set: function (value) {
                if ($scope.$collection) {
                  $scope.$collection[$scope.$index] = value;
                }
              },
              enumerable: true
            });

            Object.defineProperty(context, '$form', {
              get: function () {
                return $scope.$form;
              },
              set: function (value) {
                $scope.$form = value;
              },
              enumerable: true
            });

            var pbPropertyValues = propertyValuesFactory.get(tAttributes.pbPropertyValues);
            bindingsFactory.create(pbPropertyValues, context, $scope.properties = {});
            $scope.properties.isBound = function (property) {
              return !!(pbPropertyValues[property] && pbPropertyValues[property].type === 'data');
            }
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
