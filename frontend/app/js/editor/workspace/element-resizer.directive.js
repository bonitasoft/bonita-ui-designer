/**
 * Resizer helps to resize a component on the left or on the right
 */
angular.module('pb.directives').directive('elementResizer', function($document, elementResizerModel, componentUtils) {

  'use strict';

  // CallBack set after compile.
  var handleMouseUp = angular.noop, toggleActiveClassName = angular.noop;

  /**
   * Set a context for the resize component
   * @param  {Object} scope   current scope
   * @param  {jQLite} element Current element
   * @return {void}
   */
  function initResizing(scope, element) {

    //In bootstrap the width is not in pixel (but in percent) so we have to compute the step which involves a width change
    //To compute the width we use the container
    elementResizerModel.colBootstrapWidth = parseInt(document.getElementById(scope.component.$$id).parentElement.getBoundingClientRect().width, 10) / 12;

    //We have 2 resizers (helper for resizing) one for resizing on the left and another for the right
    elementResizerModel.left = element.get(0).querySelector('.element-resizer-left');
    elementResizerModel.right = element.get(0).querySelector('.element-resizer-right');

    elementResizerModel.bootstrapWidth = componentUtils.width.get(scope.component);
    elementResizerModel.bootstrapNewWidth = componentUtils.width.get(scope.component);
    //We saved the mouse position when the user clicks on the component
    elementResizerModel.startX = 0;

    elementResizerModel.left.style.width = '1px';
    elementResizerModel.right.style.width = '1px';
    elementResizerModel.right.style.visibility = 'hidden';
    elementResizerModel.left.style.visibility = 'hidden';
  }

  $document.on('mousemove', function (e) {
    if(elementResizerModel.isResisableComponent) {
      /**
       * When the user move the border of the resizer we calculate the new size
       */
      var resizeWidth = elementResizerModel.startX - e.clientX;
      var resizeWidthInBootstrap = parseInt(resizeWidth / elementResizerModel.colBootstrapWidth, 10);

      if (resizeWidth > 0) {
        //we go on the left
        elementResizerModel.toggleVisibility('left');
        elementResizerModel.resize('left', resizeWidthInBootstrap * elementResizerModel.colBootstrapWidth);
      } else {
        //we go on the right
        elementResizerModel.toggleVisibility('right');
        elementResizerModel.resize('right', -resizeWidthInBootstrap * elementResizerModel.colBootstrapWidth);
      }

      elementResizerModel.computeCols(resizeWidthInBootstrap);
    }
  });

  $document.on('mouseup', function (e) {

    if(elementResizerModel.isResisableComponent) {
      handleMouseUp(e);
      toggleActiveClassName();
    }
  });


  $document.on('mousedown', function (e) {
    if(e.target.className.indexOf('element-resizer') > -1) {
      e.preventDefault();

      elementResizerModel.isResisableComponent = true;
      elementResizerModel.startX = e.clientX;
      toggleActiveClassName();
    }
  });

  return {
    restrict: 'E',
    scope: {
      component: '=',
      editor: '=',
      resizable: '='
    },
    template: '<div class="element-resizer"><div class="element-resizer-left"></div><div class="element-resizer-right"></div></div> ',
    link: function(scope, element) {

      //If no element is present, we return
      if (!scope.resizable) {
        return;
      }

      //We call the init
      initResizing(scope, element);

      /**
       * Toggle to the current resizable component a className: component-resize-active
       * @return {void}
       */
      toggleActiveClassName = function toggleActiveClassName() {
        angular.element(document.getElementById(scope.component.$$id)).toggleClass('component-resize-active');
      };

      /**
       * The user releases the button and we have to compute the final width
       * Create the callback postCompile;
       */
      handleMouseUp = function handleMouseUp(event) {
        //The event propagation is prevented
        event.stopPropagation();
        event.preventDefault();

        //If the bootstrap width is changed we modify the component
        if (elementResizerModel.bootstrapNewWidth !== elementResizerModel.bootstrapWidth && scope.editor.isCurrentComponent(scope.component)) {
          //We change limit if they are outbounds
          if (elementResizerModel.bootstrapNewWidth < 1) {
            elementResizerModel.bootstrapNewWidth = 1;
          }
          else if (elementResizerModel.bootstrapNewWidth > 12) {
            elementResizerModel.bootstrapNewWidth = 12;
          }

          //We call the editor to refresh the width
          componentUtils.width.set(scope.component, Math.abs(elementResizerModel.bootstrapNewWidth));

          // we trigger the apply to make sure our changes are propagated
          scope.$apply();
          elementResizerModel.isResisableComponent = false;
        }

        //We reinitialize the resizers
        initResizing(scope, element);
      };

    }
  };
});
