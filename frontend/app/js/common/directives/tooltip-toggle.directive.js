/**
 * Wrap ace directive according to page builder needs
 */


angular.module('pb.directives').directive('tooltipToggle', function($timeout) {
  'use strict';

  return {
    name: 'tooltipToggle',
    link: function(scope, element, attr) {
       attr.tooltipTrigger = 'show-tooltip';

       scope.$watch(attr.tooltipToggle, function(value) {
        if (value) {
          // tooltip provider will call scope.$apply, so need to get out of this digest cycle first
          $timeout(function() {
            element.triggerHandler('show-tooltip');
          });
        }
        else {
          $timeout(function() {
            element.triggerHandler('hide-tooltip');
          });
        }
      });
    }
  };
});
