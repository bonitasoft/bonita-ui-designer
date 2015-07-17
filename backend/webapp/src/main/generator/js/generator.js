(function() {
  'use strict';

  angular.module('bonitasoft.ui.constants', []);
  angular.module('bonitasoft.ui.services', []);
  angular.module('bonitasoft.ui.directives', []);
  angular.module('bonitasoft.ui.widgets', []);
  angular.module('bonitasoft.ui.extensions', []);

  angular.module('bonitasoft.ui.generator', [
    'ngSanitize',
    'ngMessages',
    'ngUpload',
    'bonitasoft.ui.templates',
    'bonitasoft.ui.constants',
    'bonitasoft.ui.services',
    'bonitasoft.ui.directives',
    'bonitasoft.ui.widgets',
    'bonitasoft.ui.extensions'
  ])
  .config(function($httpProvider) {
    $httpProvider.interceptors.push('httpActivityInterceptor');
  });
})();

