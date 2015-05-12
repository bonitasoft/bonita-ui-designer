angular.module('pb.directives').directive('componentMover', function() {

  'use strict';

  return {
    restrict: 'E',
    replace: true,
    scope: {
      component: '=',
      onDelete: '&'
    },
    templateUrl: 'js/editor/workspace/component-mover.html',
    controller: 'ComponentMoverDirectiveCtrl'
  };
});
