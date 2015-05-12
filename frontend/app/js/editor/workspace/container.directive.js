/**
 * Container is an element directive allowing to display a container in the page editor. The container is either
 * the top-lever one, or a sub-container contained in a parent container cell.
 */
angular.module('pb.directives').directive('container', function(RecursionHelper) {

  'use strict';

  return {
    restrict: 'E',
    scope: {
      id: '@',
      container: '=',
      editor: '=',
      component: '='
    },
    templateUrl: 'js/editor/workspace/container.html',
    controller: 'ContainerDirectiveCtrl',
    compile: function(element) {
      return RecursionHelper.compile(element, function() {
      });
    }
  };
});
