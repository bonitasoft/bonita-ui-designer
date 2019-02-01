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

  angular
    .module('bonitasoft.designer.preview')
    .controller('PreviewCtrl', PreviewCtrl);

  /**
   * The preview controller. It handles the loading of the page model, the resolution changes and provides
   * common functions to the directives used inside the page.
   */
  function PreviewCtrl($scope, $sce, $location, $httpParamSerializer, $window, $log, iframeParameters, resolutions, webSocket, clock, artifactRepo, $state, mode, $http, $localStorage, gettextCatalog) {

    const NO_APP_TOKEN = gettextCatalog.getString('default-bonita-appName');
    const NO_APP_DISPALY_NAME = gettextCatalog.getString('No application selected');

    $scope.iframe = {};
    $scope.refreshIframe = refreshIframe;
    $scope.buildIframeSrc = buildIframeSrc;
    $scope.iframeWidth = iframeWidth;
    $scope.updateResolutionInUrl = updateResolutionInUrl;
    $scope.isNavCollapsed = true;
    $scope.pathToLivingApp = NO_APP_TOKEN;
    $scope.selectedAppDisplayName = '';
    $scope.apps = [];
    $scope.saveToStorageAndRefresh = saveToStorageAndRefresh;

    artifactRepo
      .load(iframeParameters.id)
      .then((response) => {
        $scope.pageName = response.data.name;
      });

    let storage = $localStorage.bonitaUIDesigner;
    if (storage && storage.bosAppName) {
      $scope.pathToLivingApp = storage.bosAppName;
      setApplicationDisplayName($scope.pathToLivingApp);
    }

    listAvailableApps();
    refreshIframe();
    webSocket.connect().then(() => {
      webSocket.subscribe('/previewableUpdates', (id) => {
        if (id === iframeParameters.id) {
          refreshIframe();
        }
      });
      webSocket.subscribe('/previewableRemoval', (id) => {
        if (id === iframeParameters.id) {
          closeWindow();
        }
      });
    }, () => $log.error('error connecting to notifications web socket for preview'));

    /**
     * The iframe source has to be a trusted url for Angular, hence the use of the $sce service.
     * We have to prefix the url with `index.html` for Firefox, or it will not display the iframe.
     */
    function buildIframeSrc() {
      return $sce.trustAsResourceUrl(iframeParameters.url + '/' + $scope.pathToLivingApp + '/' + iframeParameters.id + '/' + buildIframeQueryString({ time: clock.now() }));
    }

    function buildIframeQueryString(additionalParams) {
      var params = angular.extend({}, $location.search(), additionalParams || {});
      var queryString = $httpParamSerializer(params);
      return queryString ? '?' + queryString : '';
    }

    function iframeWidth() {
      return resolutions.selected().width;
    }

    function refreshIframe() {
      $scope.iframe.src = buildIframeSrc();
    }

    function closeWindow() {
      $window.close();
    }

    function updateResolutionInUrl(resolution) {
      $state.go(`designer.preview`, {
        resolution: resolution.key,
        id: iframeParameters.id,
        mode: mode
      });
    }

    function listAvailableApps() {
      $http.get('/API/living/application?preview=true&c=100').then((list) => $scope.apps = list.data);
    }

    function setApplicationDisplayName(appToken) {
      if (NO_APP_TOKEN !== appToken) {
        $http.get('/API/living/application?preview=true&c=1&p=0&f=token=' + appToken).then((response) => {
          $scope.selectedAppDisplayName = response.data[0].displayName;
        });
      } else {
        $scope.selectedAppDisplayName = NO_APP_DISPALY_NAME;
      }
    }

    function saveToStorageAndRefresh(newAppToken) {
      if (!$localStorage.bonitaUIDesigner) {
        $localStorage.bonitaUIDesigner = {};
      }
      $localStorage.bonitaUIDesigner.bosAppName = newAppToken;
      setApplicationDisplayName(newAppToken);
      $scope.refreshIframe();
    }
  }
})();
