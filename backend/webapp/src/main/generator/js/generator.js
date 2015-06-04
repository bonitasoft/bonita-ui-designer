(function() {
  'use strict';

  angular.module('pb.generator.constants', []);
  angular.module('pb.generator.services', []);
  angular.module('pb.generator.directives', []);
  angular.module('pb.widgets', []);

  angular.module('pb.generator', [
    'ngSanitize',
    'ngMessages',
    'ngUpload',
    'pb.generator.templates',
    'pb.generator.constants',
    'pb.generator.services',
    'pb.generator.directives',
    'pb.widgets'
  ]);
})();

