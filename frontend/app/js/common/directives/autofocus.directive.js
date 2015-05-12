angular.module('pb.directives')
  .directive('autofocus', function ($timeout) {

    'use strict';

    // Because autofocus does not work as expected with angular :/
    return {
      restrict: 'A',
      require: '?aceEditor',
      link: function(scope, element, attr, ctrl) {
        var input = element.get(0);
        if(attr.autofocus) {
          // If the previous state was false and new one is true focus dat input
          scope.$watch(attr.autofocus, function (newVal, oldVal) {
            if(!oldVal && newVal) {
              // To be trigger after the watch...
              $timeout(function() {
                input.focus();
                if (input.type === 'text' && input.value.length > 0 ) {
                  input.select();
                }
              });
            }
          });
        }


        // If you do not depend of a property to watch as the defautl autofocus works only once with angular
        if(!attr.autofocus) {

          $timeout(function() {
            if (ctrl) {
              ctrl.editor.focus();
            } else {
              input.focus();
              if (input.type === 'text' && input.value.length > 0 ) {
                input.select();
              }
            }
          });
        }
      }
    };

  });
