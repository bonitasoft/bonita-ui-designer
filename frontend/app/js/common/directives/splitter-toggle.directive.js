/**
 * add click event to element to trigger event to toggle a sidebar
 */
angular.module('pb.directives').directive('splitterToggle', function() {

  /**
   * Get event name according to target splitter type
   * @param {Element} splitter  targetted splitter
   * @returns {string}  the corresponding event name to trigger
   */
  function getEventName (splitter) {
    if (splitter.hasAttribute('splitter-horizontal')) {
      return 'splitter:toggle:bottom';
    } else if (splitter.hasAttribute('splitter-vertical')) {
      return splitter.getAttribute('splitter-vertical') === 'left' ? 'splitter:toggle:left' : 'splitter:toggle:right';
    } else {
      throw 'splitterToggle can only be applied to splitterHorizontal and splitterVertical';
    }
  }

  return {
    link: function($scope, $element, $attrs) {
      var eventName = getEventName(document.querySelector($attrs.splitterToggle));
      $element.on('click', function() {
        angular.element($attrs.splitterToggle).trigger(eventName);
      });
    }
  };
});
