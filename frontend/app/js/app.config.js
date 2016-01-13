/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {

  'use strict';

  // configure ace base paths
  ace.config.set('basePath', 'js');
  ace.config.set('modePath', 'js');
  ace.config.set('themePath', 'js');
  ace.config.set('workerPath', 'js');

  var isIE9 = window.navigator.userAgent.indexOf('MSIE 9') > -1;

  angular.module('uidesigner', ['bonitasoft.designer'])
    .value('isIE9', isIE9)
    .config(configureModule)
    .run(($rootScope, $uibModalStack) => {

      // Close modals on location changes
      $rootScope.$on('$locationChangeStart', () => $uibModalStack.dismissAll());
    });

  /* @ngInject */
  function configureModule($compileProvider, boDraggableItemProvider, $uibTooltipProvider, $stateProvider, $urlRouterProvider, appStates, $uibModalProvider) {

    /**
     * For the build, gulp replaces '%debugMode%' by false. For the dev no need to replace, it's eval to true.
     * {@link https://docs.angularjs.org/guide/production}
     */
    $compileProvider.debugInfoEnabled('%debugMode%');

    boDraggableItemProvider.cloneOnDrop(false);
    boDraggableItemProvider.activeBodyClassName(true);

    /* create custom trigger for popover */
    $uibTooltipProvider.setTriggers({
      'show-tooltip': 'hide-tooltip'
    });

    /* set default url */
    $urlRouterProvider.otherwise('/en/home');

    /* create ui-router states */
    Object.keys(appStates).forEach(function(stateName) {
      $stateProvider.state(stateName, appStates[stateName]);
    });

    angular.extend($uibModalProvider.options, {
      backdrop: 'static',
      //BS-14199 : change modal appearance animation for IE not to put cursor anywhere during animation
      windowClass: 'modal fade in'
    });

  }
})();
