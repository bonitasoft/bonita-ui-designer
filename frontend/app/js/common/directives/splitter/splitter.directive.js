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
(() => {
  'use strict';

  class Splitter {

    constructor($document, $scope) {
      this.resizer = document.getElementById($scope.splitterId);
      this.document = $document;
      this.leftSide = this.resizer.previousElementSibling;
      this.rightSide = this.resizer.nextElementSibling;

      // The current position of mouse
      this.x = 0;
      this.y = 0;
      // Width of left side
      this.leftWidth = 0;
      // Attach the handler
      this.resizer.addEventListener('mousedown', this.mouseDownHandler.bind(this));
    }

    // Handle the mousedown event
    // that's triggered when user drags the resizer
    mouseDownHandler(e) {
      // Get the current mouse position
      this.x = e.clientX;
      this.y = e.clientY;
      this.leftWidth = this.leftSide.getBoundingClientRect().width;
      // Attach the listeners to `document`
      this.document.on('mousemove', this.mouseMoveHandler.bind(this));
      this.document.on('mouseup', this.mouseUpHandler.bind(this));
    };

    mouseMoveHandler(e) {
      // How far the mouse has been moved
      const dx = e.clientX - this.x;
      // const dy = e.clientY - y;
      const newLeftWidth = ((this.leftWidth + dx) * 100) / this.resizer.parentNode.getBoundingClientRect().width;
      this.leftSide.style.width = `${newLeftWidth}%`;
    };

    mouseUpHandler() {
      this.resizer.style.removeProperty('cursor');
      document.body.style.removeProperty('cursor');

      this.leftSide.style.removeProperty('user-select');
      this.leftSide.style.removeProperty('pointer-events');

      this.rightSide.style.removeProperty('user-select');
      this.rightSide.style.removeProperty('pointer-events');

      // Remove the handlers of `mousemove` and `mouseup`
      this.document.off('mousemove');
      this.document.off('mouseup');
      window.dispatchEvent(new Event('resize'));
    };


  }
  angular
    .module('bonitasoft.designer.common.directives')
    .directive('splitter',  () => ({
      restrict: 'E',
      replace: true,
      scope:{
        splitterId : '@'
      },
      template: '<div class="resizer" id="splitter"></div>',
      controller: Splitter
    }));
})();
