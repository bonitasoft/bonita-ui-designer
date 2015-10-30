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
 * Vertical splitter based on an absolute positioning
 * Allow to resize, close, open two panes
 *
 * Panes should be passed via attributes pane-left and pane-right by css selectors
 */
angular.module('bonitasoft.designer.common.directives')
  .directive('splitterVertical', function($document) {

    return {
      scope: {
        paneLeft: '@',
        paneRight: '@',
        paneRightMax: '@',
        paneRightMin: '@'
      },
      transclude: true,
      template: '<div class="splitter splitter-vertical"></div><ng-transclude></ng-transclude>',
      controller: function($scope, $window) {
        this.displayed = true;
        this.xPosition = undefined;

        this.leftElem = $($scope.paneLeft);
        this.rightElem = $($scope.paneRight);

        /**
         * Close the right pane
         */
        this.closeRight = function() {
          var rightBounds = this.rightElem[0].getBoundingClientRect();
          this.xPosition = rightBounds.right - rightBounds.left;

          this.rightElem.addClass('splitter-pane-closed');
          this.leftElem.css({ right: 0 }).addClass('splitter-closed-right');
        };

        /**
         * Open the right pane
         */
        this.openRight = function() {
          this.leftElem.css({ right: this.xPosition + 'px' }).removeClass('splitter-closed-right');
          this.rightElem.removeClass('splitter-pane-closed');
        };

        /**
         * Close the left pane
         */
        this.closeLeft = function() {
          var leftBounds = this.leftElem[0].getBoundingClientRect();
          this.xPosition = leftBounds.right;

          this.rightElem.css({ left: 0 }).addClass('splitter-closed-left');
          this.leftElem.addClass('splitter-pane-closed');
        };

        /**
         * Open the left pane
         */
        this.openLeft = function() {
          this.rightElem.css({ left: this.xPosition + 'px' }).removeClass('splitter-closed-left');
          this.leftElem.removeClass('splitter-pane-closed');
        };

        this.toggleRight = function() {
          if (this.displayed) {
            this.closeRight();
          } else {
            this.openRight();
          }
          this.displayed = !this.displayed;
        };

        this.toggleLeft = function() {
          if (this.displayed) {
            this.closeLeft();
          } else {
            this.openLeft();
          }

          this.displayed = !this.displayed;
        };

        /**
         * Compute new absolute x positioning according to min and max size
         * @param {number} pointerX  current pointer x position
         * @returns {number}
         */
        function computeX(pointerX) {
          var x = pointerX;
          if ($window.innerWidth - x > $scope.paneRightMax) {
            x = $window.innerWidth - $scope.paneRightMax;
          }
          if ($window.innerWidth - x < $scope.paneRightMin) {
            x = $window.innerWidth - $scope.paneRightMin;
          }
          return x;
        }

        /**
         * Resize the two panes
         * @param {number} pointerX  current pointer x position
         */
        this.resize = function(pointerX) {
          this.xPosition = computeX(pointerX);
          this.rightElem.css({ left: this.xPosition + 'px' });
          this.leftElem.css({ right: ($window.innerWidth - this.xPosition) + 'px' });
        };
      },
      link: function($scope, $element, $attrs, $ctrl) {

        var paneLeft = $($attrs.paneLeft);
        var paneRight = $($attrs.paneRight);

        paneLeft.addClass('splitter-pane splitter-pane-left');
        paneRight.addClass('splitter-pane splitter-pane-right');

        $element.find('.splitter').on('mousedown', function(event) {
          event.preventDefault();

          // delegate event to document beacause when moving mouse we go out of the splitter
          $document.on('mousemove', mousemove);
          $document.on('mouseup', mouseup);
        });

        $element.on('splitter:toggle:right', function(event) {
          event.preventDefault();
          $ctrl.toggleRight();
        });

        $element.on('splitter:toggle:left', function(event) {
          event.preventDefault();
          $ctrl.toggleLeft();
        });

        function mousemove(event) {
          paneLeft.addClass('splitter-onmove');
          paneRight.addClass('splitter-onmove');
          $ctrl.resize(event.pageX);
        }

        // unbind events
        function mouseup() {
          paneLeft.removeClass('splitter-onmove');
          paneRight.removeClass('splitter-onmove');
          $document.unbind('mousemove', mousemove);
          $document.unbind('mouseup', mouseup);
        }
      }
    };
  });
