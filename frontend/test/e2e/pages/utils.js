(function() {
  'use strict';

  module.exports = {
    mouseOver,
    switchToAlert
  };

  /**
   * Simulate mouse over on the element
   */
  function mouseOver(element) {
    browser.actions().mouseMove(element, {x: 1, y: 1}).perform();
  }

  /**
   * Based on
   * https://github.com/angular/protractor/issues/1486
   * https://github.com/angular/angular.js/commit/addb1ae37d775937688ae6f09e6f0ebd79225849
   */
  function switchToAlert() {
    browser.wait(protractor.ExpectedConditions.alertIsPresent(), 1000);
    return browser.switchTo().alert();
  }
})();
