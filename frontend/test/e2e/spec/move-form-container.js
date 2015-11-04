var PageEditor = require('../pages/editor.page.js');

describe('moving form container', function() {

  var editor;
  beforeEach(function() {
    editor = PageEditor.get('empty');

    editor.addFormContainer();
    editor.setWidth('3');

    editor.addFormContainer();
    editor.setWidth('4');

    editor.addFormContainer();
    editor.setWidth('5');

    // root container + 3 sub-containers
    expect(editor.containers.count()).toBe(4);

  });

  it('should not allow moving first container left', function() {
    var firstContainer = editor.formContainers.first();

    firstContainer.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(firstContainer).perform();
    expect(firstContainer.element(by.css('.move-left')).isPresent()).toBe(false);
    expect(firstContainer.element(by.css('.move-right')).isPresent()).toBe(true);
  });

  it('should not allow moving last container right', function() {
    var lastContainer = editor.formContainers.last();

    lastContainer.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(lastContainer).perform();
    expect(lastContainer.element(by.css('.move-left')).isPresent()).toBe(true);
    expect(lastContainer.element(by.css('.move-right')).isPresent()).toBe(false);
  });

  it('should allow moving a container left', function() {
    var secondContainer = editor.formContainers.get(1);
    expect(secondContainer.element(by.css('.move-left')).isPresent()).toBe(false);

    secondContainer.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(secondContainer).perform();
    secondContainer.element(by.css('.move-left')).click();
    expect(editor.formContainers.first().element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
  });

  it('should allow moving a container right', function() {
    var secondContainer = editor.formContainers.get(1);
    expect(secondContainer.element(by.css('.move-right')).isPresent()).toBe(false);

    secondContainer.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(secondContainer).perform();
    secondContainer.element(by.css('.move-right')).click();
    expect(editor.formContainers.last().element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
  });
});
