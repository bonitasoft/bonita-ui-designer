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

      this.$onInit = function(){
        this.tabs = [
          tabFactory.create({
            order:0,
            name: gettext('Variables'),
            stateName: `designer.${this.mode}`,
            bottomPanel: this,
            icon:'fa fa-link'
          }),

        ];

        if (this.mode === 'page') {
          this.tabs.push(tabFactory.create({
            order:1,
            name: gettext('Assets'),
            stateName: 'designer.page.asset',
            bottomPanel: this,
            icon: 'fa fa-file'
          }));
        }
        // We don't add the Bonita Resource panel for form artifact
        if (this.isForm !== 'true') {
          this.tabs.push(tabFactory.create({
            order:2,
            name: gettext('Bonita resources'),
            stateName: `designer.${this.mode}.webResources`,
            bottomPanel: this,
            icon: 'fa fa-fg fa-lock'
          }));
        }
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

  angular
    .module('bonitasoft.designer.editor.bottom-panel')
    .component('bottomPanel', {
      bindings: { mode: '@', isForm: '@' },
      templateUrl: 'js/editor/bottom-panel/bottom-panel.html',
      controller: BottomPanelController,
      controllerAs: 'bottomPanel'
    });
}());
