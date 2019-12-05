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

  function AppSelectorCtrl($scope, gettextCatalog, $localStorage, $http, appSelectorService, $log) {

    var vm = this;
    vm.pathToLivingApp = appSelectorService.getPathToLivingApp();
    vm.apps = [];

    retrieveAvailableUserApps();

    function setFilteredApps(userProfiles, allApps) {
      if (userProfiles.length !== 0) {
        const userProfilesIds = userProfiles.map(profile => profile.id);
        vm.apps = allApps.filter((app) => userProfilesIds.indexOf(app.profileId) >= 0);
      }
    }

    // jscs: disable requireCamelCaseOrUpperCaseIdentifiers
    function retrieveAvailableUserApps() {
      let allApps;
      $http.get('./API/living/application?preview=true&c=200')
        .catch(() => {
          // If user is not connected
          setDefaultPathToLivingApp();
          setApplicationDisplayName(vm.pathToLivingApp);
        }).then((response) => {
          if (response) {
            allApps = response.data;
            return $http.get('./API/system/session/unusedId');
          }
        })
        .then((response) => {
          if (response) {
            const userID = response.data.user_id; /* jshint ignore:line */
            return $http.get('./API/portal/profile?p=0&c=200&f=user_id%3d' + userID);
          }
        })
        .then((response) => {
          if (response) {
            setFilteredApps(response.data, allApps);
            initializeSelection(vm.apps);
          } else {
            $log.error('Cannot connect to the Bonita server. Check a user is logged in.');
          }
        });
    }
    // jscs: enable requireCamelCaseOrUpperCaseIdentifiers

    function initializeSelection(appList) {
      if (!isAppExistInList(appSelectorService.getPathToLivingApp(), appList)) {
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
