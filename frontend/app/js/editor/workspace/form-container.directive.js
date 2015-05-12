angular.module('pb.directives')
  .directive('formContainer', function(){

    'use strict';

    return {
      restrict: 'E',
      scope: {
        id: '@',
        formContainer: '=',
        editor: '='
      },
      templateUrl: 'js/editor/workspace/form-container.html'
    };
});
