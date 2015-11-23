(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.home')
    .directive('homeFilters', homeFilters);

  function homeFilters() {
    return {
      scope: {
        filters: '='
      },
      templateUrl: 'js/home/home-filters.html'
    };
  }

})();
