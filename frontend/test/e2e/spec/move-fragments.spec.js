var PageEditor = require('../pages/editor.page.js');

describe('moving fragments test', function() {

  var rootContainer, editor;

  beforeEach(function() {
    editor = PageEditor.get('empty');

    editor.addFragment('personFragment');
    editor.setWidth('3');

    editor.addFragment('personFragment');
    editor.setWidth('4');

    editor.addFragment('personFragment');
    editor.setWidth('5');

    rootContainer = editor.containers.first();
  });

  it('should not allow moving first fragment left', function() {
    var firstFragment = rootContainer.all(by.css('fragment')).first();
    firstFragment.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(firstFragment).perform();
    expect(firstFragment.element(by.css('.move-left')).isPresent()).toBe(false);
    expect(firstFragment.element(by.css('.move-right')).isPresent()).toBe(true);
  });

  it('should not allow moving last fragment right', function() {
    var lastFragment = rootContainer.all(by.css('fragment')).last();
    lastFragment.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(lastFragment).perform();
    expect(lastFragment.element(by.css('.move-left')).isPresent()).toBe(true);
    expect(lastFragment.element(by.css('.move-right')).isPresent()).toBe(false);
  });

  it('should allow moving a fragment left', function() {
    var secondFragment = rootContainer.all(by.css('fragment')).get(1);
    //button should not be displayed
    expect(secondFragment.element(by.css('.move-left')).isPresent()).toBe(false);
    secondFragment.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(secondFragment).perform();

    secondFragment.element(by.css('.move-left')).click();
    expect(editor.fragments.first().element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
  });

  it('should allow moving a fragment right', function() {
    var secondFragment = rootContainer.all(by.css('fragment')).get(1);
    //button should not be displayed
    expect(secondFragment.element(by.css('.move-right')).isPresent()).toBe(false);
    secondFragment.click();
    //simulate mouseover to display the component toolbar
    browser.actions().mouseMove(secondFragment).perform();

    secondFragment.element(by.css('.move-right')).click();
    expect(editor.fragments.last().element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');

  });
});
