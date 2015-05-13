/**
 * Element directive displaying a widget in the palette, with just its label for now.
 */
angular.module('pb.directives').directive('paletteWidget', function() {

  'use strict';

  return {
    restrict: 'E',
    scope: {
      widget: '='
    },
    templateUrl: 'js/editor/palette/palette-widget.html',
    link: function(scope) {
      scope.$watch('widget.component.icon', function(icon) {
        var blankIcon = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"></svg>';
        scope.iconData = 'data:image/svg+xml,' + encodeURIComponent(icon||blankIcon);
      });
    }
  };
});
