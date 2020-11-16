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
    .run(($rootScope, $uibModalStack, resolutions, gettext, features, $localStorage) => {

      // Allow to enabled/disable experimental mode
      features.init().then(function() {
        $localStorage.experimentalMode = false;

        if (features.isExperimentalMode()) {
          $localStorage.experimentalMode = features.isExperimentalMode();
        }
      });

      resolutions.registerResolutions([
        {
          key: 'xs',
          label: 'Phone',
          icon: 'mobile',
          width: 320,
          tooltip: gettext('Extra Small devices (width \u003C 768px)')
        },
        {
          key: 'sm',
          label: 'Tablet',
          icon: 'tablet',
          width: 768,
          tooltip:  gettext('Small devices (width \u2265 768px)')
        },
        {
          key: 'md',
          label: 'Desktop',
          icon: 'laptop',
          width: 992,
          tooltip:  gettext('Medium devices (width \u2265 992px)')
        },
        {
          key: 'lg',
          label: 'Large desktop',
          icon: 'desktop',
          width: 1200,
          tooltip:  gettext('Large devices (width \u2265 1200px)')
        }
      ]);
      resolutions.setDefaultResolution('md');
      resolutions.setDefaultDimension({ xs: 12, sm: 12, md: 12, lg: 12 });

      // Close modals on location changes
      $rootScope.$on('$locationChangeStart', () => $uibModalStack.dismissAll());
    });

  /* @ngInject */
  function configureModule($compileProvider, boDraggableItemProvider, $uibTooltipProvider, $urlRouterProvider, assetsServiceProvider, $uibModalProvider) {

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

    assetsServiceProvider.registerType({
      key: 'json',
      value: 'Localization',
      filter: true,
      widget: false,
      template: 'js/assets/l10n-asset-form.html',
      aceMode: 'json',
      orderable: false
    });

    angular.extend($uibModalProvider.options, {
      backdrop: 'static',
      //BS-14199 : change modal appearance animation for IE not to put cursor anywhere during animation
      windowClass: 'modal fade in'
    });
  }
})();
