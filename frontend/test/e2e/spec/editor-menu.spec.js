var PageEditor = require('../pages/editor.page.js');

describe('editor menu', function() {

  'use strict';

  var editor;

  it('should save a page', function() {
    editor = PageEditor.get('empty');

    // given a page with two containers, each with one label and an input
    // add container
    editor.addContainer();
    editor.setWidth('6');

    // add a label in the container
    editor.addElement('pbParagraph').to('#pbContainer-0 .widget-placeholder');
    editor.setWidth('4');

    // add an input in the container
    editor.addElement('pbInput').to('#pbContainer-0 .widget-placeholder');
    editor.setWidth('8');

    // add another container
    editor.addContainer();
    editor.setWidth('6');

    // add a label in the container
    editor.addElement('pbParagraph').to('#pbContainer-1 .widget-placeholder');
    editor.setWidth('4');

    // add an input in the container
    editor.addElement('pbInput').to('#pbContainer-1 .widget-placeholder');
    editor.setWidth('8');

    // add a name and save
    $('#pageName').sendKeys('person');

    expect(editor.saveIndicator.isDisplayed()).toBeFalsy();
    $('#save').click();
    expect($('#save').isEnabled()).toBeFalsy();
  });

  it('should load a page', function() {
    PageEditor.get('personPage');
    // then we should have a page with two containers, each with one label and an input
    expect($$('#pbContainer-0').count()).toBe(1);
    expect($$('#pbContainer-0 label').count()).toBe(1);
    expect($$('#pbContainer-0 input').count()).toBe(1);
    expect($$('#pbContainer-1').count()).toBe(1);
    expect($$('#pbContainer-1 label').count()).toBe(1);
    expect($$('#pbContainer-1 input').count()).toBe(1);

    // we should have the correct size
    // we use element(by.xpath('..')) to get the parent node
    expect($('#pbContainer-0').element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
    expect($('#pbContainer-0').element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
    expect($('#pbContainer-1').element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
    expect($('#pbContainer-1').element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
  });

  it('should allow to edit page data', function() {
    var editor = PageEditor.get('personPage');

    var dataPanel = editor.dataPanel();

    expect(dataPanel.addButton.isPresent()).toBeTruthy();
  });

  it('should move an item to another row', function() {
    var editor = PageEditor.get('personPage');

    editor.drag('.component:first').andDropOn('.dropRow--last',0,1);
    expect(editor.rows.count()).toBe(2);

    editor.drag('.component:first').andDropOn('.dropRow--last',0,1);
    expect(editor.rows.count()).toBe(3);
  });

  it('should drop an item next to another in the same row', function() {

    var editor = PageEditor.get('personPage');

    editor.drag('.component:first').andDropOn('#component-4 .widget-wrapper:first-child .dropZone--right');

    // we use element(by.xpath('..')) to get the parent node
    expect($('#component-4').element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
    expect($('#component-4').element(by.xpath('../..')).getAttribute('class')).toContain('col-xs-4');
    expect($$('.component + .component #component-4').count()).toBe(1);

    editor.drag('.component:first').andDropOn('#component-4 .widget-wrapper:first-child .dropZone--right');

    expect($$('.component:first-of-type #component-4').count()).toBe(1);
  });

  it('should save a page as', function() {
    var editor = PageEditor.get('personPage');

    editor.menu.$('button.dropdown-toggle').click();
    editor.menu.$('#saveAs').click();

    var newName = $('.modal-body input[name="name"]');
    var submitButton = $('.modal-footer .btn-primary');
    var typePage = $('.modal-body input#type-page');
    var typeForm = $('.modal-body input#type-form');

    // input should be filled with current page name
    expect(newName.getAttribute('value')).toBe('Person');
    expect(typePage.getAttribute('checked')).toBeTruthy();
    expect(typeForm.getAttribute('checked')).toBeFalsy();

    // button disabled when we enter a wrong page name
    newName.clear();
    newName.sendKeys('wrong page name');
    expect(submitButton.isEnabled()).toBeFalsy();

    // page is renamed and url is updated
    newName.clear();
    newName.sendKeys('GoodNewName');
    submitButton.click();
    expect(browser.getCurrentUrl()).toMatch(/.*\/pages\/GoodNewName/);   // http://host:port/#/en/pages/GoodNewName  => in e2e config page name = page id
  });

  it('should edit page metadata', function() {
    var editor = PageEditor.get('personPage');

    editor.menu.$('#metadata').click();

    var displayName = $('.modal-body input[name="displayName"]');
    var description = $('.modal-body textarea[name="description"]');
    var submitButton = $('.modal-footer .btn-primary');


    // button enable when no display name
    displayName.clear();
    expect(submitButton.isEnabled()).toBeTruthy();

    // display name and description are changed
    displayName.clear();
    displayName.sendKeys('new display name');
    description.clear();
    description.sendKeys('new description');

    expect(submitButton.isEnabled()).toBeTruthy();
    submitButton.click();

    editor.menu.$('#metadata').click();

    var newDisplayName = $('.modal-body input[name="displayName"]');
    expect(newDisplayName.getAttribute('value')).toBe('new display name');
    var newDescription = $('.modal-body textarea[name="description"]');
    expect(newDescription.getAttribute('value')).toBe('new description');


    $('.modal-footer .btn-link').click();
  });

  it('should convert page', function() {
    var editor = PageEditor.get('personPage');

    editor.menu.$('button.dropdown-toggle').click();
    editor.menu.$('#convert').click();

    var typePage = $('.modal-body input#type-page');
    var typeForm = $('.modal-body input#type-form');
    var submitButton = $('.modal-footer .btn-primary');

    expect(submitButton.isEnabled()).toBeTruthy();

    expect(typePage.getAttribute('checked')).toBeTruthy();
    expect(typeForm.getAttribute('checked')).toBeFalsy();

    typeForm.click();

    expect(submitButton.isEnabled()).toBeTruthy();
    submitButton.click();

    editor.menu.$('button.dropdown-toggle').click();
    editor.menu.$('#convert').click();

    var newTypeForm = $('.modal-body input#type-form');
    expect(newTypeForm.getAttribute('checked')).toBeTruthy();

    $('.modal-footer .btn-link').click();
  });

});
