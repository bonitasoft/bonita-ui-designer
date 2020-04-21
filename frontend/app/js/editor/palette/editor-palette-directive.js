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
    .module('bonitasoft.designer.editor.palette')
    .controller('EditorPaletteCtrl', EditorPaletteCtrl)
    .directive('editorPalette', editorPaletteDirective);

  /**
   * Element directive displaying a widget in the palette, with just its label for now.
   */
  function editorPaletteDirective() {
    return {
      restrict: 'A',
      scope: {
        onResize: '&'
      },
      controller: 'EditorPaletteCtrl',
      controllerAs: 'palette',
      templateUrl: 'js/editor/palette/editor-palette.html'
    };
  }

  function EditorPaletteCtrl($scope, paletteService, $uibModal, $window, $http) {

    var palette = this;
    /**
     * The palette contains all the widgets that can be added to the page.
     */
    this.sections = paletteService.getSections();
    this.currentSection = this.sections[0];
    this.toggleSection = toggleSection;
    this.isActiveSection = isActiveSection;
    this.isClosed = isClosed;
    this.getIconClassName = getIconClassName;
    this.isFragment = isFragment;
    this.isDataSection = isDataSection;
    this.$http = $http;
    this.bdrUrl = '';
    resize();

    $scope.$on('fragmentCreated', function() {
      // Update fragments section
      updateSections();
    });

    function toggleSection(section) {
      palette.currentSection = palette.currentSection === section ? undefined : section;
      resize();
    }

    function isActiveSection(section) {
      return palette.currentSection && palette.currentSection.name === section.name;
    }

    function resize() {
      $scope.onResize({
        isClosed: isClosed()
      });
    }

    function isClosed() {
      return palette.currentSection === undefined;
    }

    function isFragment() {
      return $scope.$parent.mode === 'fragment';
    }

    function getIconClassName(section) {
      return 'ui-' + section.name.replace(/ /g, '');   // remove white spaces
    }

    function updateSections() {
      palette.sections = paletteService.getSections();
      palette.sections.forEach(section => {
        if (isActiveSection(section)) {
          palette.currentSection = section;
        }
      });
      resize();
    }

    function isDataSection(section) {
      return section.type === 'data';
    }

    this.openHelp = function() {
      $uibModal.open({
        templateUrl: 'js/editor/palette/data-model-help-popup.html',
        size: 'md'
      });
    };

    this.openVoyager = function() {
      if (!this.bdrUrl) {
        let config = {
          headers: {
            'Content-Type': 'application/json',
          }
        };
        return this.$http.get('rest/bdr/url', config)
          .then((bdrUrl) => {
            this.bdrUrl = bdrUrl.data.url + '/bdm/graphical';
            this._openVoyagerWindow();
          });
      } else {
        this._openVoyagerWindow();
      }
    };

    this._openVoyagerWindow = function() {
      $window.open(this.bdrUrl, '_blank');
    };

  }
})();
