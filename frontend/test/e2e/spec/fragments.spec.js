import PageEditor from '../pages/editor.page.js';
import DataPanel from '../pages/data-panel.page.js';

describe('fragment test', function() {

  var editor;
  beforeEach(function() {
    editor = PageEditor.get('empty');
  });

  it('should allow to save a container as a fragment', function() {
    // given a container
    editor.addContainer();
    // with a label
    editor.addElement('pbParagraph').to('.widget-placeholder');
    editor.setWidth(4);

    // and an input
    editor.addElement('pbInput').to('.widget-placeholder');
    editor.setWidth(8);

    var container = editor.containersInEditor.first();
    browser.actions().mouseMove(container, { x: 1, y: 1 }).click().perform();

    $('#widgetActionButton').click();
    $('#saveAsFragmentAction').click();
    $('#fragment-name').sendKeys('person');
    $('#save-fragment').click();

    // then we should have a fragment
    expect($$('.fragment-content').count()).toBe(1);

    // that we can remove
    editor.removeWidget();

    // and it should not be there anymore
    expect($$('.fragment-content').count()).toBe(0);
  });

  it('should discard save as window on browser navigation', function() {
    $('button.dropdown-toggle').click();
    $('#saveAs').click();

    browser.setLocation('/');

    expect($('.modal-footer').isPresent()).toBeFalsy();
  });

  it('should allow to save a container as a fragment with ENTER', function() {
    // given a container
    editor.addContainer();
    // with a label
    editor.addElement('pbParagraph').to('.widget-placeholder');
    editor.setWidth(4);

    // and an input
    editor.addElement('pbInput').to('.widget-placeholder');
    editor.setWidth(8);

    // when we save as a fragment
    var container = editor.containersInEditor.first();
    browser.actions().mouseMove(container, { x: 1, y: 1 }).click().perform();

    $('#widgetActionButton').click();
    $('#saveAsFragmentAction').click();
    var input = $('#fragment-name');
    input.sendKeys('person');
    input.sendKeys(protractor.Key.ENTER);

    // then we should have a fragment
    expect($$('.fragment-content').count()).toBe(1);

    // that we can remove
    editor.removeWidget();

    // and it should not be there anymore
    expect($$('.fragment-content').count()).toBe(0);
  });

  it('should focus on fragment name input', function() {
    // given a container
    editor.addContainer();
    // with a label
    editor.addElement('pbParagraph').to('.widget-placeholder');
    editor.setWidth(4);

    // and an input
    editor.addElement('pbInput').to('.widget-placeholder');
    editor.setWidth(8);

    // when we save as a fragment
    var container = editor.containersInEditor.first();
    browser.actions().mouseMove(container, { x: 1, y: 1 }).click().perform();
    $('#widgetActionButton').click();
    $('#saveAsFragmentAction').click();

    // to wait 500ms+
    browser.driver.sleep(600);

    // using the Protractor 'element' helper
    // https://github.com/angular/protractor/blob/master/docs/api.md#element
    // var input = element(by.id('foo'));
    var input = $('#fragment-name');

    expect(input.getAttribute('id')).toEqual(browser.driver.switchTo().activeElement().getAttribute('id'));
  });

  it('should allow to add a fragment and edit it', function() {
    // given a container
    editor.addFragment('personFragment');
    // then we should be able to edit it
    $('#widgetActionButton').click();
    $('#editFragmentAction').click();

    // then we should go the fragment edition
    expect($('.EditorHeader-brand').getText()).toEqual('FRAGMENT EDITOR');
    expect($('#pageName').getAttribute('value')).toEqual('personFragment');
    expect($$('component').count()).toBe(2);
  });


  //TODO: See fragment into fragment
  it('should see fragment in a fragment', function() {
    editor.addFragment('fragWithTitleFrag');

    expect($('fragment[fragment-name="simpleFragment"] h2').getText()).toEqual('{{title}}');
  });

  it('should display default properties on container', function() {
    editor.addFragment('personFragment');
    expect(element(by.cssContainingText('.property-label label', 'CSS classes')).isPresent()).toBe(true);
    expect(element(by.cssContainingText('.property-label label', 'Hidden')).isPresent()).toBe(true);
  });

  describe('fragment editor', function() {
    it('should display exposed data type with css', function(){
      browser.get('#/en/fragments/personFragment');
      let dataPanel = new DataPanel();
      let exposedData = dataPanel.getDataByName('user');
      expect(exposedData.type).toBe('(Exposed)');
      expect(dataPanel.lines.get(1).$('.VariablesTable-type').getAttribute('class')).toMatch('VariablesTable-type--exposed');
    });

    it('should display none exposed data type without css', function(){
      browser.get('#/en/fragments/personFragment');
      let dataPanel = new DataPanel();
      let noneExposedData = dataPanel.getDataByName('admin');
      expect(noneExposedData.type).toBe('String');
      expect(dataPanel.lines.get(0).$('.VariablesTable-type').getAttribute('class')).not.toMatch('VariablesTable-type--exposed');
    });
  });

});
