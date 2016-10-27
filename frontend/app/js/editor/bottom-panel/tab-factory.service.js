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

  class Tab {

    constructor({ $state, name, stateName, bottomPanel }) {
      this.$state = $state;
      this.name = name;
      this.stateName = stateName;
      this.bottomPanel = bottomPanel;
    }

    isActive() {
      return this.$state.current.name === this.stateName && !this.bottomPanel.isClosed();
    }

    activate() {
      if (this.bottomPanel.isClosed()) {
        this.bottomPanel.open();
      } else if (this.isActive()) {
        this.bottomPanel.close();
      }
      this.$state.go(this.stateName, undefined, { location: false });
    }
  }

  angular.module('bonitasoft.designer.editor.bottom-panel').service('tabFactory', ($state) => ({
    create({ name, stateName, bottomPanel }) {
      return new Tab({ $state, name, stateName, bottomPanel });
    }
  }));
}());
