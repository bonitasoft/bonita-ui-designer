/**
 * The preview controller. It handles the loading of the page model, the resolution changes and provides
 * common functions to the directives used inside the page.
 */
angular.module('pb.preview').controller('PreviewCtrl', function($scope, $sce, $stateParams, iframeParameters, resolutions, webSocket, clock, artifactRepo) {

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
    return $sce.trustAsResourceUrl(iframeParameters.url + '/' + iframeParameters.id + '?time=' + clock.now());
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

  $scope.resolution = function() {
    return resolutions.selected();
  };

});
