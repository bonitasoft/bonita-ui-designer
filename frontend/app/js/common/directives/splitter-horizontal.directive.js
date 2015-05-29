/**
 * Horizontal splitter based on an absolute positioning
 * Allow to resize, close, open two panes
 *
 * Panes should be passed via attributes pane-top and pane-bottom by css selectors
 */
angular.module('pb.directives')
  .directive('splitterHorizontal', function($document, $state) {

    return {
      scope: {
        paneTop: '@',
        paneBottom: '@',
        paneBottomMax: '@',
        paneBottomMin: '@',
        closedOnInit: '@',
        defaultState: '@'
      },
      transclude: true,
      template: '<div class="BottomPanel-splitter"></div>',
      controller: function($scope) {
        this.displayed = true;
        this.topElem = $($scope.paneTop);
        this.bottomElem = $($scope.paneBottom);

        /**
         * Compute new absolute y positioning according to min and max size
         * @param {number} bottom bottom bound of the bottom pane
         * @param {number} pointerY  current pointer y position
         * @returns {number}
         */
        function computeY (bottom, pointerY) {
          var y = bottom - pointerY;
          if (y > $scope.paneBottomMax) {
            y = $scope.paneBottomMax;
          }
          if (y < $scope.paneBottomMin) {
            y = $scope.paneBottomMin;
          }
          return y;
        }

        /**
         * Resize the two panes
         * @param {number} pointerY  current pointer y position
         */
        this.resize = function(pointerY) {
          var y = computeY(this.bottomElem[0].getBoundingClientRect().bottom, pointerY);
          this.topElem.css({bottom: y + 'px'});
          this.bottomElem.css({height: y + 'px'});
        };

        /**
         * Close the bottom pane
         */
        this.closeBottom = function() {
          this.topElem.css({bottom: 0});
          this.bottomElem.addClass('splitter-pane-closed');
          this.displayed = false;
        };

        /**
         * Open the bottom pane
         */
        this.openBottom = function() {
          this.bottomElem.removeClass('splitter-pane-closed');
          this.topElem.css({bottom: this.bottomElem[0].getBoundingClientRect().height + 'px'});
          this.displayed = true;
        };

        this.toggleBottom = function() {
          if (this.displayed) {
            this.closeBottom();
          } else {
            this.openBottom();
          }
        };
      },
      link: function($scope, $element, $attrs, $ctrl) {
        var currentState = $attrs.defaultState;
        var paneTop = $($attrs.paneTop);
        var paneBottom = $($attrs.paneBottom);
        paneTop.addClass('splitter-pane splitter-pane-top');
        paneBottom.addClass('splitter-pane splitter-pane-bottom');
        if (!!$scope.$eval($attrs.closedOnInit) ) {
          $ctrl.closeBottom();
        }

        $element.find('.BottomPanel-splitter').on('mousedown', function(event) {
          event.preventDefault();

          // delegate event to document beacause when moving mouse we go out of the splitter
          $document.on('mousemove', mousemove);
          $document.on('mouseup', mouseup);
        });

        // unbind events
        function mouseup () {
          paneTop.removeClass('splitter-onmove');
          paneBottom.removeClass('splitter-onmove');
          $document.unbind('mousemove', mousemove);
          $document.unbind('mouseup', mouseup);
        }

        function mousemove (event) {
          paneTop.addClass('splitter-onmove');
          paneBottom.addClass('splitter-onmove');
          $ctrl.resize(event.pageY);
        }

        $element.on('splitter:toggle:bottom', function(event, targetState) {
          event.preventDefault();
          //When the state didn't change it's a panel toggle or panel is hidden
          if(currentState === targetState && $ctrl.displayed || ! $ctrl.displayed){
            $ctrl.toggleBottom();
          }

          if(currentState !== targetState) {
            //Call ui-router to change state
            currentState = targetState;
            $state.go(currentState);
          }

        });
      }
    };
  });
