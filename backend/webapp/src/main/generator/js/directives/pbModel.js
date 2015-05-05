(function () {
  'use strict';

  angular.module('org.bonitasoft.pagebuilder.generator.directives')
    .directive('pbModel', function ($parse, modelFactory, bindingsFactory, dataModelFactory) {

      function PbModelCtrl() {}
      PbModelCtrl.prototype.fill = function (rawData) {
        var model = modelFactory.create(rawData);
        this.createGateway = model.createGateway;
        this.getModel = function() {
          return model;
        };
      };

      function pbModelCompile(tElement, tAttributes) {
        var pbModelProperties = $parse(tAttributes.pbModelProperties)();
        // avoid having angular interpolating property values as well
        tElement.removeAttr("pbModelProperties");

        return {
          pre: function (scope, element, attrs, pbModelCtrl) {
            pbModelCtrl.fill(dataModelFactory.get(tAttributes.pbModel));
            if(pbModelProperties && scope.$parent.pbModelCtrl) {
              bindingsFactory.create(pbModelProperties, scope.$parent.pbModelCtrl.createGateway(), pbModelCtrl.getModel());
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
