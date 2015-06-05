(function() {

  'use strict';

  angular.module('pb.filters', []);
  angular.module('pb.controllers', ['pb.filters']);
  angular.module('pb.services', ['pb.filters', 'gettext']);
  angular.module('pb.factories', ['pb.filters']);
  angular.module('pb.directives', ['pb.filters']);
  angular.module('pb.widgets', []);

  angular.module('pb', [
    'app.route',
    'pb.preview',
    'pb.home',
    'pb.custom-widget',
    'pb.common.repositories',
    'pb.common.services',
    'pb.controllers',
    'pb.factories',
    'pb.services',
    'pb.directives',
    'pb.filters',
    'pb.templates',
    'pb.assets',
    'ngSanitize',
    'ui.router',
    'RecursionHelper',
    'ui.bootstrap',
    'ui.ace',
    'pb.widgets',
    'org.bonitasoft.dragAndDrop',
    'gettext',
    'ngUpload',
    'angularMoment'
  ]);
})();
