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
  angular.module('bonitasoft.designer.app-selector').service('appSelectorService', AppSelectorService);

  function AppSelectorService($http, gettextCatalog, $localStorage) {

    'use strict';

    const NO_APP_SELECTED_DISPLAY_NAME = gettextCatalog.getString('No application selected');
    const NO_APP_SELECTED_PATH = 'no-app-selected';
    let pathToLivingApp = NO_APP_SELECTED_PATH;

    let storage = $localStorage.bonitaUIDesigner;
    if (storage && storage.bosAppName) {
      pathToLivingApp = storage.bosAppName;
    } else {
      $localStorage.bonitaUIDesigner = {};
      $localStorage.bonitaUIDesigner.bosAppName = pathToLivingApp;
    }

    function savePathToLivingApp(newPathToLivingApp) {
      pathToLivingApp = newPathToLivingApp;
      saveToStorage(newPathToLivingApp);
    }

    function getPathToLivingApp() {
      return pathToLivingApp;
    }

    function saveToStorage(newPathToLivingApp) {
      $localStorage.bonitaUIDesigner.bosAppName = newPathToLivingApp;
    }

    function getDefaultAppSelection() {
      return {
        token: NO_APP_SELECTED_PATH,
        displayName: NO_APP_SELECTED_DISPLAY_NAME
      };
    }

    return {
      getDefaultAppSelection,
      savePathToLivingApp,
      getPathToLivingApp
    };
  }
})();
