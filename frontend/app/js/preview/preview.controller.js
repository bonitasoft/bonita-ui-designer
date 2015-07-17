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
/**
 * The preview controller. It handles the loading of the page model, the resolution changes and provides
 * common functions to the directives used inside the page.
 */
angular.module('bonitasoft.ui.preview').controller('PreviewCtrl', function($scope, $sce, $stateParams, iframeParameters, resolutions, webSocket, clock, artifactRepo) {

  'use strict';

  artifactRepo
    .load(iframeParameters.id)
    .then(function (response) {
      $scope.pageName = response.data.name;
    });

  /**
   * The iframe source has to be a trusted url for Angular, hence the use of the $sce service.
   * We have to prefix the url with `index.html` for Firefox, or it will not display the iframe.
   */
  $scope.buildIframeSrc = function() {
    return $sce.trustAsResourceUrl(iframeParameters.url + '/' + iframeParameters.id + '/?time=' + clock.now());
  };

  $scope.iframeWidth = function() {
    return resolutions.selected().width;
  };

  $scope.iframe = {
    src: $scope.buildIframeSrc()
  };

  /**
   * Refreshes the iframe if it's the current preview
   * @param id - the id of updated artifact
   */
  this.wsCallback = function(id) {
    if (id === iframeParameters.id) {
      $scope.iframe.src = $scope.buildIframeSrc();
    }
  };

  webSocket.listen('/previewableUpdates', this.wsCallback);

});
