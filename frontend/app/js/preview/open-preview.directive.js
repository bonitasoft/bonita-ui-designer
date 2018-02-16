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
 * Open a popup with the preview of your page
 */
class OpenPreviewCtrl {
  constructor($window, $state, resolutions, keyBindingService) {
    this.$window = $window;
    this.$state = $state;
    this.resolutions = resolutions;
    this.keyBindingService = keyBindingService;
  }

  openPreview() {
    if (this.onOpenPreview) {
      this.onOpenPreview()
        .then(newArtifactId => {
          if (newArtifactId) {
            this.artifactId = newArtifactId;
            if (this.previewWindow && !this.previewWindow.closed) {
              this.previewWindow.location = this.getPreviewLocation();
            }
          }
        });
    }
    //cannot be done when onOpenPreview promise is resolved as browser are blocking async window.open
    this.handlePreviewWindow();
  }

  handlePreviewWindow() {
    if (this.previewWindow && !this.previewWindow.closed) {
      this.previewWindow.focus();
    } else {
      //Preview window is opened with a toolbar=1 option to make the URL editable (only works on Firefow unfortunately)
      //See https://bugs.chromium.org/p/chromium/issues/detail?id=82522
      //This is an improvement for BS-16078
      this.previewWindow = this.$window.open(this.getPreviewLocation(),
        'preview', 'width=1024,height=768,toolbar=1,resizable=1,scrollbars=1');
    }
  }

  getPreviewLocation() {
    return this.$state.href(`designer.preview`, {
      resolution: this.resolutions.selected().key,
      id: this.artifactId,
      mode: this.mode
    });
  }
}

angular.module('bonitasoft.designer.preview')
  .directive('openPreview', () => ({
    controllerAs: 'ctrl',
    controller: OpenPreviewCtrl,
    bindToController: {
      onOpenPreview: '&',
      artifactId: '=',
      mode: '@',
      isDisabled: '='
    },
    scope: true,
    restrict: 'E',
    templateUrl: 'js/preview/open-preview.html'
  }));
