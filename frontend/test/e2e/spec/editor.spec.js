var PageEditor = require('../pages/editor.page.js');

describe('editor test', function() {

  var editor;
  beforeEach(function() {
    editor = PageEditor.get('empty');
  });
  it('should display a hint message when editor is empty', function() {
    var hint = $('.alert-editor-hint');

    expect(hint.isPresent()).toBe(true);
    expect(hint.getText()).toMatch('This page is empty.');
  });

  it('should allow adding a widget when dropping on #editor', function() {
    editor.addElement('pbParagraph').to('#editor');
    expect(editor.components.count()).toBe(1);
    expect(editor.components.get(0).element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-12');
  });

  it('should allow adding widget and set its width', function() {
    expect(editor.palette.isPresent()).toBe(true);

    editor.addWidget('pbParagraph');
    expect(editor.components.count()).toBe(1);
    // we use element(by.xpath('..')) to get the parent node
    expect(editor.components.get(0).element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-12');

    editor.setWidth(6);

    // we use element(by.xpath('..')) to get the parent node
    expect(editor.components.get(0).element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-6');
  });

  it('should allow removing a widget using DEL keyboard shortcut', function() {
    editor.addWidget('pbParagraph');

    expect(editor.components.count()).toBe(1);
    browser.actions().sendKeys(protractor.Key.DELETE).perform();
    expect(editor.components.count()).toBe(0);
  });

  it('should not allow removing a widget using DEL keyboard shortcut if no widget is selected', function() {
    editor.addWidget('pbParagraph');
    editor.addElement('pbParagraph').to('.dropRow--last');
    editor.removeWidget();
    expect(editor.components.count()).toBe(1);
    browser.actions().sendKeys(protractor.Key.DELETE).perform();
    expect(editor.components.count()).toBe(1);
  });

  it('should move selection to the next component using RIGHT', function() {
    editor.addWidget('pbParagraph');
    editor.addElement('pbParagraph').to('.dropRow--last');
    editor.components.first().click();
    browser.actions().sendKeys(protractor.Key.RIGHT).perform();
    expect(editor.components.last().getAttribute('class')).toContain('component-element--selected');
  });

  it('should leave selection using RIGHT to the last component if is already selected', function() {
    editor.addWidget('pbParagraph');
    editor.addElement('pbParagraph').to('.dropRow--last');
    browser.actions().sendKeys(protractor.Key.RIGHT).perform();
    expect(editor.components.last().getAttribute('class')).toContain('component-element--selected');
  });

  it('should move selection to the previous component using LEFT', function() {
    editor.addWidget('pbParagraph');
    editor.addElement('pbParagraph').to('.dropRow--last');
    browser.actions().sendKeys(protractor.Key.LEFT).perform();
    expect(editor.components.first().getAttribute('class')).toContain('component-element--selected');
  });

  it('should leave selection using LEFT to the last component if is already selected', function() {
    editor.addWidget('pbParagraph');
    editor.addElement('pbParagraph').to('.dropRow--last');
    editor.components.first().click();
    browser.actions().sendKeys(protractor.Key.LEFT).perform();
    expect(editor.components.first().getAttribute('class')).toContain('component-element--selected');
  });

  it('should allow removing a widget', function() {
    editor.addWidget('pbParagraph');

    expect(editor.components.count()).toBe(1);

    editor.removeWidget();
    expect(editor.components.count()).toBe(0);
  });

  it('should fill widget placeholder when page is empty', function() {
    editor.addWidget('pbParagraph');

    expect(editor.components.count()).toBe(1);
    expect($$('.widget-placeholder').count()).toBe(0);
  });

  it('should not allow removing the unique row', function() {
    expect($('.builder-row .removeRow').isPresent()).toBe(false);
  });

  it('should allow removing a non-unique row', function() {
    editor.addWidget('pbParagraph');
    editor.addElement('pbParagraph').to('.dropRow--last');

    browser
      .executeScript('$(\'.row-builder .fa-times-circle\').first().click();')
      .then(function() {
        expect(editor.rows.count()).toBe(1);
      });
  });

  it('should allow adding container and set its width', function() {
    // only the root container initially
    expect($$('container').count()).toBe(1);

    editor.addContainer();
    expect($$('container').count()).toBe(2);
    // we use element(by.xpath('..')) to get the parent node
    expect($$('container').get(1).element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-12');

    editor.setWidth(6);
    // we use element(by.xpath('..')) to get the parent node
    expect($$('container').get(1).element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-6');
  });

  it('should allow adding container on the left of a component', function() {
    // only the root container initially
    expect($$('container').count()).toBe(1);

    editor.addWidget('pbParagraph');
    // add a container before the widget
    editor.addElement('container').to('.dropZone', true, false);
    expect($$('container').count()).toBe(2);

    // container should be before component
    var elements = $$('[data-is-editor-container="true"] .component');
    expect(elements.get(0).element(by.tagName('container')).isDisplayed()).toBe(true);
    expect(elements.get(1).element(by.tagName('component')).isDisplayed()).toBe(true);
  });

  it('should allow adding container on the right of a component', function() {
    // only the root container initially
    expect($$('container').count()).toBe(1);

    editor.addWidget('pbParagraph');
    // add a container after the widget
    editor.addElement('container').to('.dropZone', false, true);
    expect($$('container').count()).toBe(2);

    // container should be after component
    var elements = $$('[data-is-editor-container="true"] .component');
    expect(elements.get(0).element(by.tagName('component')).isDisplayed()).toBe(true);
    expect(elements.get(1).element(by.tagName('container')).isDisplayed()).toBe(true);
  });

  it('should allow removing a container', function() {
    editor.addContainer();

    expect($$('container').count()).toBe(2);

    editor.removeWidget();
    expect($$('container').count()).toBe(1);
  });

  it('should allow adding tabs container and set its width', function() {
    editor.addTabsContainer();

    expect($$('tabs-container').count()).toBe(1);
    // we use element(by.xpath('..')) to get the parent node
    expect($$('tabs-container').get(0).element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-12');

    editor.setWidth(6);
    // we use element(by.xpath('..')) to get the parent node
    expect($$('tabs-container').get(0).element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-6');
  });

  it('should allow removing a tabs container', function() {
    editor.addTabsContainer();
    expect($$('tabs-container').count()).toBe(1);

    editor.removeWidget();
    expect($$('tabs-container').count()).toBe(0);
  });

  it('should allow adding form container and set its width', function() {
    editor.addFormContainer();

    expect($$('form-container').count()).toBe(1);
    // we use element(by.xpath('..')) to get the parent node
    expect($$('form-container').get(0).element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-12');

    editor.setWidth(6);
    // we use element(by.xpath('..')) to get the parent node
    expect($$('form-container').get(0).element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-6');
  });

  it('should allow removing a forms container', function() {
    editor.addFormContainer();
    expect($$('form-container').count()).toBe(1);

    editor.removeWidget();
    expect($$('form-container').count()).toBe(0);
  });

  it('should allow adding a custom widget', function() {
    editor.addCustomWidget('customAwesomeWidget');

    expect($('#component-0 .widget-content').getInnerHtml()).toContain('My awesome widget just rocks!');

    editor.removeWidget();
    expect($$('#component-0').count()).toBe(0);
  });

  it('should open help popup',  function() {
    $('.btn-bonita-help').click();

    expect($('.modal-header .modal-title').getText()).toBe('Help');
    expect($('#help-responsiveness').isPresent()).toBeFalsy();
  });
});
