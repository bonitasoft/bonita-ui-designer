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
 * Element directive allowing to display a tabs container in the preview.
 */
angular.module('bonitasoft.designer.editor.whiteboard').directive('tabsContainerPreview', function() {

  'use strict';

  return {
    restrict: 'E',
    scope: {
      tabsContainer: '=',
      preview: '='
    },
    templateUrl: 'js/editor/whiteboard/fragment-preview/tabs-container-preview.html',
    link: function(scope) {
      scope.tabsContainer.$$openedTab = scope.tabsContainer.tabList[0];
      scope.isOpened = function(tab) {
        return tab === scope.tabsContainer.$$openedTab;
      };
      scope.openTab = function(tab) {
        scope.tabsContainer.$$openedTab = tab;
      };
    }
  };
});
