var PageEditor = require('../pages/editor.page.js');

describe('moving components test', function() {

  var editor;
  beforeEach(function() {
    editor = PageEditor.get('empty');

    // add a label with width 1
    editor.addWidget('pbParagraph');
    editor.setWidth(1);

    // add a label with width 2
    editor.addWidget('pbParagraph');
    editor.setWidth(2);

    // add a label with width 3
    editor.addWidget('pbParagraph');
    editor.setWidth(3);

    expect(editor.components.count()).toBe(3);
  });

  it('should not allow moving first component left', function() {
    var firstComponent = editor.components.first();
    firstComponent.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(firstComponent).perform();
    expect(firstComponent.element(by.css('.move-left')).isPresent()).toBe(false);
    expect(firstComponent.element(by.css('.move-right')).isPresent()).toBe(true);
  });

  it('should not allow moving last component right', function() {
    var lastComponent = editor.components.last();
    lastComponent.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(lastComponent).perform();
    expect(lastComponent.element(by.css('.move-left')).isPresent()).toBe(true);
    expect(lastComponent.element(by.css('.move-right')).isPresent()).toBe(false);
  });

  it('should allow moving a component left', function() {
    var secondComponent = editor.components.get(1);
    expect(secondComponent.element(by.css('.move-left')).isPresent()).toBe(false);
    secondComponent.click();

    //simulate mouseover to display the component toolbars
    browser.actions().mouseMove(secondComponent.element(by.css('.move-left'))).click().perform();
    expect(editor.components.first().element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-2');
  });

  it('should allow moving a component right', function() {
    var secondComponent = editor.components.get(1);
    expect(secondComponent.element(by.css('.move-right')).isPresent()).toBe(false);

    secondComponent.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(secondComponent.element(by.css('.move-right'))).click().perform();
    expect(editor.components.last().element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-2');
  });

  it('should not allow moving an existing component on #editor', function() {
    // element is necessary to force the promise resolution
    var workspace = $('#editor');
    var height;
    workspace.getSize().then(function(size) {
      height = size.height;
    });

    editor.drag('.component:first-child').andDropOn('#editor');
    expect(editor.rows.count()).toBe(1);
  });
});
