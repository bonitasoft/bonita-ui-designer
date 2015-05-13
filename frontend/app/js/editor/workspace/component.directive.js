/**
 * Component is an element directive allowing to display a widget component in the page editor.
 * It wraps the widget in a div containing also an overlay that will be displayed whenever the user
 * enter the div with his mouse, and hidden when he left.
 */
angular.module('pb.directives').directive('component', function ($compile, componentScopeBuilder) {

  'use strict';

  return {
    restrict: 'E',
    scope: {
      component: '=',
      editor: '=',
      componentIndex: '=',
      container: '=',
      row: '=',
      resizable: '='
    },
    link: function (scope, element) {
      var componentScope = componentScopeBuilder.build(scope);

      if(scope.component.$$widget) {
        // insert the html template in the div with class widget-content
        var div = angular.element(element.get(0).querySelector('.widget-content'));
        var widgetDomElement = $compile(scope.component.$$widget.template)(componentScope);
        div.append(widgetDomElement);
      }

    },
    templateUrl: 'js/editor/workspace/component.html'
  };
});
