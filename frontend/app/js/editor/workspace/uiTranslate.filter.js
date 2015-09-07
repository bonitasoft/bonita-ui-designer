(function () {
  'use strict';

  angular.module('bonitasoft.designer.filters')

    // Disable widget internationalization whenever we are in the whiteboard
    .filter('uiTranslate', function () {
      return function (value) {
        return value;
      };
    });
})();
