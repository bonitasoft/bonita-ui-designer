(function() {

  'use strict';

  // configure ace base paths
  ace.config.set('basePath', 'js');
  ace.config.set('modePath', 'js');
  ace.config.set('themePath', 'js');
  ace.config.set('workerPath', 'js');

  var isIE9 = window.navigator.userAgent.indexOf('MSIE 9') > -1;

  angular.module('uidesigner', ['pb'])
    .value('isIE9', isIE9)
    .config(configureModule);

    /* @ngInject */
    function configureModule($compileProvider, boDraggableItemProvider, $tooltipProvider, $stateProvider, $urlRouterProvider, appStates) {

      /**
       * For the build, gulp replaces '%debugMode%' by false. For the dev no need to replace, it's eval to true.
       * {@link https://docs.angularjs.org/guide/production}
       */
      $compileProvider.debugInfoEnabled('%debugMode%');

      boDraggableItemProvider.cloneOnDrop(false);
      boDraggableItemProvider.activeBodyClassName(true);

      /* create custom trigger for popover */
      $tooltipProvider.setTriggers({
        'show-tooltip': 'hide-tooltip'
      });

      /* set default url */
      $urlRouterProvider.otherwise('/en/home');

      /* create ui-router states */
      Object.keys(appStates).forEach(function(stateName){
        $stateProvider.state(stateName, appStates[stateName]);
      });

    }
})();
