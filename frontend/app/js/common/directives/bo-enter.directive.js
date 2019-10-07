
angular.module('bonitasoft.designer.common.directives')
  .directive('boEnter', function() {
  return function(scope, element, attrs) {
    element.bind('keydown keypress', function(event) {
      if (event.which === 13) {
        scope.$apply(function() {
          scope.$eval(attrs.boEnter);
        });

        event.preventDefault();
      }
    });
  };
});
