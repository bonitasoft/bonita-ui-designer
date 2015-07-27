(function() {
  'use strict';

  angular.module('bonitasoft.ui.constants', []);
  angular.module('bonitasoft.ui.services', []);
  angular.module('bonitasoft.ui.directives', []);
  angular.module('bonitasoft.ui.widgets', []);
  angular.module('bonitasoft.ui.extensions', []);

  /* keep the former main module name for backward compatibility reasons*/
  angular.module('pb.constants', []);
  angular.module('pb.services', []);
  angular.module('pb.directives', []);
  angular.module('pb.widgets', []);
  angular.module('pb.generator', []);


  angular.module('bonitasoft.ui', [
    'ngSanitize',
    'ngMessages',
    'ngUpload',
    'bonitasoft.ui.templates',
    'bonitasoft.ui.constants',
    'bonitasoft.ui.services',
    'bonitasoft.ui.directives',
    'bonitasoft.ui.widgets',
    'bonitasoft.ui.extensions',
    'pb.generator'
  ])
  .config(function($httpProvider) {
    $httpProvider.interceptors.push('httpActivityInterceptor');
  });
})();

