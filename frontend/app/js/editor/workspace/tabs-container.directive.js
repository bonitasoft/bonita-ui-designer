/**
 * Element directive allowing to display a tabs container in the page editor.
 */
angular.module('pb.directives').directive('tabsContainer', function() {

  'use strict';

  return {
    restrict: 'E',
    scope: {
      tabsContainer: '=',
      editor: '='
    },
    controller: 'TabsContainerDirectiveCtrl',
    templateUrl: 'js/editor/workspace/tabs-container.html'
  };
});
