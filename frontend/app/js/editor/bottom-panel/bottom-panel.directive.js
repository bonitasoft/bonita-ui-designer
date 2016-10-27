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

  class BottomPanelController {

    constructor(gettext, tabFactory) {
      this.isBottomPanelClosed = false;

      this.tabs = [
        tabFactory.create({
          name: gettext('Variables'),
          stateName: `designer.${this.mode}`,
          bottomPanel: this
        })
      ];

      if (this.mode === 'page') {
        this.tabs.push(tabFactory.create({
          name: gettext('Assets'),
          stateName: 'designer.page.asset',
          bottomPanel: this
        }));
      }
    }

    isClosed() {
      return this.isBottomPanelClosed;
    }

    open() {
      this.isBottomPanelClosed = false;
    }

    close() {
      this.isBottomPanelClosed = true;
    }
  }

  angular.module('bonitasoft.designer.editor.bottom-panel').directive('bottomPanel', () => ({
    restrict: 'E',
    scope: {},
    bindToController: { mode: '@' },
    templateUrl: 'js/editor/bottom-panel/bottom-panel.html',
    controller: BottomPanelController,
    controllerAs: 'bottomPanel'
  }));
}());
