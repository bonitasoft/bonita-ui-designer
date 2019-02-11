/*******************************************************************************
 * Copyright (C) 2009, 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.app-selector')
    .directive('appSelector', appSelector);

  function appSelector() {
    return {
      scope: {
        onChange: '&'
      },
      restrict: 'E',
      templateUrl: 'js/preview/app-selector/app-selector.html',
      controller: AppSelectorCtrl,
      controllerAs: 'vm',
      bindToController: true
    };
  }

  function AppSelectorCtrl($scope, gettextCatalog, $localStorage, $http, appSelectorService) {

    var vm = this;
    vm.pathToLivingApp = appSelectorService.getPathToLivingApp();
    vm.apps = [];

    selectorInitialization();

    function selectorInitialization() {
      $http.get('./API/living/application?preview=true&c=200').then((list) => {
        vm.apps = list.data;
        initializeSelection(list.data);
      }).catch(() => {
        // If user is not connected
        setDefaultPathToLivingApp();
        setApplicationDisplayName(vm.pathToLivingApp);
      });
    }

    function initializeSelection(appList) {
      if (!isAppExistInList(appSelectorService.getPathToLivingApp, appList)) {
        setDefaultPathToLivingApp();
      }
      setApplicationDisplayName(vm.pathToLivingApp);
    }

    function isAppExistInList(app, appList) {
      for (let i = 0; i < appList.length; i++) {
        if (appList[i].token === app) {
          return true;
        }
      }
      return false;
    }

    function setDefaultPathToLivingApp() {
      vm.pathToLivingApp = appSelectorService.getDefaultAppSelection().token;
      appSelectorService.savePathToLivingApp(vm.pathToLivingApp);
    }

    $scope.setNewAppTokenAndRefresh = function(newAppToken) {
      appSelectorService.savePathToLivingApp(newAppToken);
      setApplicationDisplayName(newAppToken);
      vm.onChange();
    };

    function setApplicationDisplayName(newAppToken) {
      if (newAppToken !== appSelectorService.getDefaultAppSelection().token) {
        $http.get('./API/living/application?preview=true&c=1&p=0&f=token=' + newAppToken).then((response) => {
          vm.selectedAppDisplayName = response.data[0].displayName;
        });
      } else {
        vm.selectedAppDisplayName = appSelectorService.getDefaultAppSelection().displayName;
      }
    }
  }

})();
