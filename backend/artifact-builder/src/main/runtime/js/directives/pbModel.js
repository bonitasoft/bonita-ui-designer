(function() {
  'use strict';

  angular.module('bonitasoft.ui.directives')
    .directive('pbModel', function($parse, modelFactory, bindingsFactory, variableModelFactory, modelPropertiesFactory, bindingContextFactory) {

      function PbModelCtrl() {}
      PbModelCtrl.prototype.fill = function(rawData) {
        var model = modelFactory.create(rawData);
        this.createGateway = model.createGateway;
        this.getModel = function() {
          return model;
        };
      };

      function pbModelCompile(tElement, tAttributes) {
        return {
          pre: function(scope, element, attrs, pbModelCtrl) {
            pbModelCtrl.fill(variableModelFactory.get(tAttributes.pbModel));
            var pbModelProperties = modelPropertiesFactory.get(tAttributes.pbModelProperties);
            if (pbModelProperties && scope.$parent.pbModelCtrl) {
              bindingsFactory.create(
                pbModelProperties,
                bindingContextFactory.create(scope.$parent.pbModelCtrl, scope),
                pbModelCtrl.getModel());
            }
          }
        };
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
        priority: 999,
        compile: pbModelCompile
      };
    });
})();
