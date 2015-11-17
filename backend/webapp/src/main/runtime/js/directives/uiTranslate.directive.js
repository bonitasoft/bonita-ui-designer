(function() {
  'use strict';

  angular.module('bonitasoft.ui.directives')
    .directive('uiTranslate', function($compile) {

      return {
        restrict: 'A',
        terminal: true, // make sure other directives won't be compiled
        priority: 1000, // make sure we run first
        compile: function(tElement, tAttrs) {
          tAttrs.$set('translate', '');
          tElement.removeAttr('ui-translate');
          return {
            post: function(scope, element) {
              $compile(element)(scope);
            }
          };
        }
      };
    });
})();
