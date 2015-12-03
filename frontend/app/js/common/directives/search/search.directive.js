(function() {
  'use strict';

  class SearchController {

    clearValue() {
      this.value = '';
    }

    isClearButtonVisible() {
      return (this.value || '').trim().length > 0;
    }
  }

  angular.module('bonitasoft.designer.common.directives').directive('search', () => ({
    scope: { value: '=', placeholder: '@' },
    bindToController: true,
    controllerAs: 'search',
    controller: SearchController,
    templateUrl: 'js/common/directives/search/search.html'
  }));
})();
