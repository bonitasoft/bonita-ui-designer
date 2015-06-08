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
angular.module('pb.factories')
  .factory('elementResizerModel', function() {

    'use strict';

    return {
      isResisableComponent: false,

      set: function set(key, value) {
        this[key] = value;
      },
      isResizable: function isResizable(active) {
        this.isResisableComponent = !!active;
      },
      computeCols: function computeCols(resizeWidthInBootstrap) {
        this.bootstrapNewWidth = this.bootstrapWidth - resizeWidthInBootstrap;
      },
      resize: function resize(item, size) {
        if(this[item]) {
          this[item].style.width = size + 'px';
        }
      },
      toggleVisibility: function toggleVisibility(item) {
        var itemReverse = ('right' === item) ? 'left' : 'right';

        if(this[item] && this[itemReverse]) {
          this[item].style.visibility = 'visible';
          this[itemReverse].style.visibility = 'hidden';
        }
      }
    };
  });
