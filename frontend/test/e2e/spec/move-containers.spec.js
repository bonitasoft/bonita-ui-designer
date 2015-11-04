var PageEditor = require('../pages/editor.page.js');

describe('moving containers test', function() {

  var rootContainer;

  var editor;
  beforeEach(function() {
    editor = PageEditor.get('empty');

    editor.addContainer();
    editor.setWidth('3');

    editor.addContainer();
    editor.setWidth('4');

    editor.addContainer();
    editor.setWidth('5');

    // root container + 3 sub-containers
    expect(editor.containers.count()).toBe(4);

    rootContainer = editor.containers.first();
  });

  it('should not allow moving first container left', function() {
    var firstContainer = rootContainer.all(by.tagName('container')).first();

    firstContainer.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(firstContainer).perform();
    expect(firstContainer.element(by.css('.move-left')).isPresent()).toBe(false);
    expect(firstContainer.element(by.css('.move-right')).isPresent()).toBe(true);
  });

  it('should not allow moving last container right', function() {
    var lastContainer = rootContainer.all(by.css('container')).last();

    lastContainer.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(lastContainer).perform();
    expect(lastContainer.element(by.css('.move-left')).isPresent()).toBe(true);
    expect(lastContainer.element(by.css('.move-right')).isPresent()).toBe(false);
  });

  it('should allow moving a container left', function() {
    var secondContainer = rootContainer.all(by.css('container')).get(1);
    expect(secondContainer.element(by.css('.move-left')).isPresent()).toBe(false);

    secondContainer.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(secondContainer).perform();
    secondContainer.element(by.css('.move-left')).click();
    expect(editor.containersInEditor.first().element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
  });

  it('should allow moving a container right', function() {
    var secondContainer = rootContainer.all(by.css('container')).get(1);
    expect(secondContainer.element(by.css('.move-right')).isPresent()).toBe(false);

    secondContainer.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(secondContainer).perform();
    secondContainer.element(by.css('.move-right')).click();
    expect(editor.containersInEditor.last().element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
  });
});
