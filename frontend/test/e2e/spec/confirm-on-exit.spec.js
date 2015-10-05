var PageEditor = require('../pages/editor.page.js');

describe('confirm on exit', function() {

  var editor;
  beforeEach(function() {
    editor = PageEditor.get('empty');
  });

  it('should not open a confirm dialog if there is no modification', function() {
    editor.back();
    expect(browser.getCurrentUrl()).toMatch(/\/home/);
  });

  it('should open a confirm dialog before going home', function() {
    editor.addWidget('pbParagraph');
    editor.back();

    var dialog = switchToAlert();
    expect(dialog.accept).toBeDefined();
    dialog.accept();
    expect(browser.getCurrentUrl()).toMatch(/\/home/);
  });

  it('should open a confirm dialog and stay on the same page if dismiss', function() {
    editor.addWidget('pbParagraph');
    editor.back();

    var dialog = switchToAlert();
    expect(dialog.dismiss).toBeDefined();
    dialog.dismiss();
    expect(browser.getCurrentUrl()).toMatch(/\/empty/);
  });

  /**
   * Based on
   * https://github.com/angular/protractor/issues/1486
   * https://github.com/angular/angular.js/commit/addb1ae37d775937688ae6f09e6f0ebd79225849
   */
  function switchToAlert() {
    browser.wait(protractor.ExpectedConditions.alertIsPresent(), 1000);
    return browser.switchTo().alert();
  }
});
