(function() {
  'use strict';

  angular.module('bonitasoft.ui.constants', []);
  angular.module('bonitasoft.ui.services', []);
  angular.module('bonitasoft.ui.directives', ['gettext']);
  angular.module('bonitasoft.ui.filters', ['gettext']);
  angular.module('bonitasoft.ui.widgets', ['bonitasoft.ui.filters', 'bonitasoft.ui.services']);
  angular.module('bonitasoft.ui.extensions', []);

  /* keep the former main module name for backward compatibility reasons*/
  angular.module('pb.widgets', []);
  angular.module('pb.generator', []);

  angular.module('bonitasoft.ui', [
    'ngSanitize',
    'ngMessages',
    'bonitasoft.ui.templates',
    'bonitasoft.ui.constants',
    'bonitasoft.ui.services',
    'bonitasoft.ui.directives',
    'bonitasoft.ui.widgets',
    'bonitasoft.ui.extensions',
    'pb.widgets',
    'pb.generator'
  ]);
})();

