(function() {
  'use strict';

  angular.module('bonitasoft.designer.editor.whiteboard')

    // Disable widget internationalization whenever we are in the whiteboard
    .filter('uiTranslate', function() {
      return function(value) {
        return value;
      };
    });
})();
