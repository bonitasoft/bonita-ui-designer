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

  function EditorPaletteCtrl($scope, paletteService) {

    var palette = this;

    /**
     * The palette contains all the widgets that can be added to the page.
     */
    this.sections = paletteService.getSections();
    this.currentSection = this.sections[0];
    this.toggleSection = toggleSection;
    this.isActiveSection = isActiveSection;
    this.isNarrow = isNarrow;
    this.isClosed = isClosed;
    this.getIconClassName = getIconClassName;

    resize();

    function toggleSection(section) {
      palette.currentSection = palette.currentSection === section ? undefined : section;
      resize();
    }

    function isActiveSection(section) {
      return palette.currentSection === section;
    }

    function resize() {
      $scope.onResize({
        isClosed: isClosed(),
        isNarrow: isNarrow()
      });
    }

    function isNarrow() {
      return !!(palette.currentSection && palette.currentSection.widgets.length < 10);
    }

    function isClosed() {
      return palette.currentSection === undefined;
    }

    function getIconClassName(section) {
      return 'ui-' + section.name.replace(/ /g, '');   // remove white spaces
    }
  }

})();
