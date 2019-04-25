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
  function PreviewCtrl($scope, $sce, $location, $httpParamSerializer, $window, $log, iframeParameters, resolutions, webSocket, clock, artifactRepo, $state, mode, appSelectorService) {
    $scope.iframe = {};
    $scope.refreshIframe = refreshIframe;
    $scope.buildIframeSrc = buildIframeSrc;
    $scope.iframeWidth = iframeWidth;
    $scope.updateResolutionInUrl = updateResolutionInUrl;
    $scope.openExpandedPreviewWindow = openExpandedPreviewWindow;
    $scope.isNavCollapsed = true;

    artifactRepo
      .load(iframeParameters.id)
      .then((response) => {
        $scope.pageName = response.data.name;
      });

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
      return $sce.trustAsResourceUrl(iframeParameters.url + '/' + appSelectorService.getPathToLivingApp() + '/' + iframeParameters.id + '/' + buildIframeQueryString({ time: clock.now() }));
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

    function openExpandedPreviewWindow() {
      $window.open($scope.iframe.src, '_blank');
    }

    function updateResolutionInUrl(resolution) {
      $state.go(`designer.preview`, {
        resolution: resolution.key,
        id: iframeParameters.id,
        mode: mode
      });
    }
  }
})();
