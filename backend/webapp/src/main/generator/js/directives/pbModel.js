(function () {
  'use strict';

  angular.module('pb.generator.directives')
    .directive('pbModel', function ($parse, modelFactory, bindingsFactory, dataModelFactory, modelPropertiesFactory) {

      function PbModelCtrl() {}
      PbModelCtrl.prototype.fill = function (rawData) {
        var model = modelFactory.create(rawData);
        this.createGateway = model.createGateway;
        this.getModel = function() {
          return model;
        };
      };

      function pbModelCompile(tElement, tAttributes) {
        return {
          pre: function (scope, element, attrs, pbModelCtrl) {
            pbModelCtrl.fill(dataModelFactory.get(tAttributes.pbModel));
            var pbModelProperties = modelPropertiesFactory.get(tAttributes.pbModelProperties);
            if(pbModelProperties && scope.$parent.pbModelCtrl) {

              var context = scope.$parent.pbModelCtrl.createGateway();
              Object.defineProperty(context, '$item', {
                get: function () {
                  return scope.$item;
                },
                set: function (value) {
                  if (scope.$collection) {
                    scope.$collection[scope.$index] = value;
                  }
                },
                enumerable: true
              });

              Object.defineProperty(context, '$form', {
                get: function () {
                  return scope.$form;
                },
                set: function (value) {
                  scope.$form = value;
                },
                enumerable: true
              });
              bindingsFactory.create(pbModelProperties, context, pbModelCtrl.getModel());
            }
          }
        }
      }

      return {
        restrict: 'A',
        controller: PbModelCtrl,
        // Expose controller to the scope to mask parent one.
        // Every time a model is created then it mask its parent
        // model allowing nested model
        controllerAs: 'pbModelCtrl',
        // This is why we use a new scope. To avoid overriding parent modelCtrl.
        scope: true,
        compile: pbModelCompile
      }
    });
})();
