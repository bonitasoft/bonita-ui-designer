var PageEditor = require('../pages/editor.page.js');

describe('tabs test', function() {

  var editor;

  beforeEach(function() {
    editor = PageEditor.get('empty');
    editor.addTabsContainer();
  });

  it('should not allow moving first tab left', function() {
    var firstTab = $$('tabs-container li a').first();
    expect(firstTab.element(by.css('.move-tab-left')).isPresent()).toBe(false);
    firstTab.click();
    expect(firstTab.element(by.css('.move-tab-left')).isPresent()).toBe(false);
  });

  it('should not allow moving last tab right', function() {
    var lastTab = $$('tabs-container li a').last();
    expect(lastTab.element(by.css('.move-tab-right')).isPresent()).toBe(false);
    lastTab.click();
    expect(lastTab.element(by.css('.move-tab-right')).isPresent()).toBe(false);
  });

  it('should allow moving a tab left', function() {
    var secondTab = $$('tabs-container li a').get(1);
    expect(secondTab.element(by.css('.move-tab-left')).isPresent()).toBe(false);
    secondTab.click();
    expect(secondTab.element(by.css('.move-tab-left')).isPresent()).toBe(true);

    secondTab.element(by.css('.move-tab-left')).click();

    expect($$('tabs-container li a').first().getText()).toBe('Tab 2');
  });

  it('should allow moving a tab right', function() {
    var firstTab = $$('tabs-container li a').first();
    expect(firstTab.element(by.css('.move-tab-right')).isPresent()).toBe(false);
    firstTab.click();
    expect(firstTab.element(by.css('.move-tab-right')).isPresent()).toBe(true);

    firstTab.element(by.css('.move-tab-right')).click();

    expect($$('tabs-container li a').get(1).getText()).toBe('Tab 1');
  });

  it('should allow removing a tab unless except the last one', function() {
    var firstTab = $$('tabs-container li a').first();
    firstTab.click();

    firstTab.element(by.css('.fa-times-circle')).click();

    expect($$('tabs-container li a').count()).toBe(2);
  });

  it('should allow adding a tab', function() {
    expect($$('tabs-container li a .tab-title').count()).toBe(2);

    var plus = $$('tabs-container li a').last();
    plus.click();

    expect($$('tabs-container li a .tab-title').count()).toBe(3);
    expect($$('tabs-container li a .tab-title').get(2).getText()).toBe('Tab 3');
  });

  it('should allow setting a tab title', function() {
    var firstTab = $$('tabs-container li a').first();
    firstTab.click();

    $('#tab-title-input').clear();
    $('#tab-title-input').sendKeys('Hello');
    expect($$('tabs-container li a').first().getText()).toBe('Hello');
  });

  it('should not disappear if I take the tabContainer and push it in itself nor its children', function() {

    //browser.pause();
    editor.drag('#tabsContainer-0').andDropOn('#tabsContainer-0 .widget-placeholder',true);
    expect($('#tabsContainer-0').isPresent()).toBe(true);

    editor.addElement('tabsContainer').to('#tabsContainer-0 .widget-placeholder',true);

    editor.drag('#tabsContainer-0').andDropOn('#tabsContainer-1 .widget-placeholder',true);
    expect($('#tabsContainer-0').isPresent()).toBe(true);
    expect($('#tabsContainer-1').isPresent()).toBe(true);

    editor.drag('#tabsContainer-1').andDropOn('#tabsContainer-1 .widget-placeholder',true);
    expect($('#tabsContainer-0').isPresent()).toBe(true);
    expect($('#tabsContainer-1').isPresent()).toBe(true);

  });

  it('should not disappear if I take the parent tabContainer and push it in a container inside of itself', function() {

    editor.addElement('container').to('#tabsContainer-0 .widget-placeholder',true);

    editor.drag('#tabsContainer-0').andDropOn('#container-2 .widget-placeholder',true);
    expect($('#tabsContainer-0').isPresent()).toBe(true);
    expect($('#container-2').isPresent()).toBe(true);

    editor.addElement('tabsContainer').to('#container-2 .widget-placeholder',true);

    editor.drag('#tabsContainer-0').andDropOn('#tabsContainer-1 .widget-placeholder',true);
    expect($('#tabsContainer-0').isPresent()).toBe(true);
    expect($('#tabsContainer-1').isPresent()).toBe(true);
    expect($('#container-2').isPresent()).toBe(true);
  });
});
