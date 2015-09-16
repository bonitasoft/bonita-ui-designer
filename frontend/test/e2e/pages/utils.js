(function() {
  'use strict';

  /**
   * Simulate mouse over on the element
   */
  function mouseOver(element) {
    browser.actions().mouseMove(element, {x: 1, y: 1}).perform();
  }

  module.exports = {
    mouseOver: mouseOver
  };
})();
