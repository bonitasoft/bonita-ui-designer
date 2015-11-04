var PageEditor = require('../pages/editor.page.js');

describe('moving tab containers test', function() {

  var editor;

  beforeEach(function() {
    editor = PageEditor.get('empty');

    editor.addTabsContainer();
    editor.setWidth(3);

    editor.addTabsContainer();
    editor.setWidth(4);

    editor.addTabsContainer();
    editor.setWidth(5);

  });

  it('should not allow moving first tabs container left', function() {

    var firstContainer = editor.tabsContainers.first();

    // Don't know why but `firstContainer.click();` do proper component selection
    browser.actions().mouseMove(firstContainer, { x: 0, y: 0 }).click().perform();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(element(by.css('.component-element--selected')),  { x: 0, y: 0 }).perform();
    expect(firstContainer.element(by.css('.move-left')).isPresent()).toBe(false);
    expect(firstContainer.element(by.css('.move-right')).isPresent()).toBe(true);
  });

  it('should not allow moving last tabs container right', function() {
    var lastContainer = editor.tabsContainers.last();
    lastContainer.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(lastContainer, { x: 1, y: 1 }).perform();
    expect(lastContainer.element(by.css('.move-left')).isPresent()).toBe(true);
    expect(lastContainer.element(by.css('.move-right')).isPresent()).toBe(false);
  });

  it('should allow moving a tabs container left', function() {
    var secondContainer = editor.tabsContainers.get(1);
    secondContainer.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(secondContainer).perform();
    secondContainer.element(by.css('.move-left')).click();
    expect(editor.tabsContainers.first().element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
  });

  it('should allow moving a tabs container right', function() {
    var secondContainer = editor.tabsContainers.get(1);
    expect(secondContainer.element(by.css('.move-right')).isPresent()).toBe(false);

    secondContainer.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(secondContainer).perform();
    secondContainer.element(by.css('.move-right')).click();
    expect(editor.tabsContainers.last().element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
  });
});
