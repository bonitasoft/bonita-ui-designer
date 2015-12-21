(function() {

  'use strict';

  //  Add e2e module to main pb module
  angular.module('uidesigner').requires.push('bonitasoft.designer.e2e');

  angular.module('bonitasoft.designer.e2e', ['ngMockE2E']);

})();
