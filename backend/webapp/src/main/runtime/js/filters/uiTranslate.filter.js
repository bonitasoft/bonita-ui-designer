(function() {
  'use strict';

  angular.module('bonitasoft.ui.filters')

    .filter('uiTranslate', function($filter) {
      return $filter('translate');
    });
})();
