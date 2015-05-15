(function() {
  'use strict';

  angular.module('org.bonitasoft.pagebuilder.generator.constants', []);
  angular.module('org.bonitasoft.pagebuilder.generator.services', []);
  angular.module('org.bonitasoft.pagebuilder.generator.directives', []);
  angular.module('org.bonitasoft.pagebuilder.widgets', []);

  angular.module('org.bonitasoft.pagebuilder.generator', [
    'ngSanitize',
    'ngMessages',
    'org.bonitasoft.pagebuilder.generator.templates',
    'org.bonitasoft.pagebuilder.generator.constants',
    'org.bonitasoft.pagebuilder.generator.services',
    'org.bonitasoft.pagebuilder.generator.directives',
    'org.bonitasoft.pagebuilder.widgets'
  ]);
})();

