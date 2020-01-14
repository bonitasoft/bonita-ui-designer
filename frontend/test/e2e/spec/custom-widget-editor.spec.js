var switchToAlert = require('../pages/utils.js').switchToAlert;
var clearAndFillAceEditor = require('../pages/utils.js').clearAndFillAceEditor;
var WidgetEditor = require('../pages/widget-editor.page');

describe('custom widget editor', function() {

  let widgetEditor;

  beforeEach(function() {
    widgetEditor = WidgetEditor.get('customAwesomeWidget');
  });

  function getPropertyNamesInList() {
    return element.all(by.css('.PropertyDescription')).map(function(elm) {
      return elm.all(by.tagName('div')).get(0).getText();
    });
  }

  function selectBond(bondName) {
    $('#bondButton').click();
    element(by.cssContainingText('#bond li', bondName)).click();
  }

  function selectType(typeName) {
    $('#typeButton').click();
    element(by.cssContainingText('#type li', typeName)).click();
  }

  it('should display title and icon', function() {
    expect($('.EditorHeader-name').getText()).toEqual('awesomeWidget');
    expect($('.EditorHeader-name img').isPresent()).toBeTruthy();
  });

  it('should display help', function() {
    $('.EditorHeader-help .btn').click();
    expect($('li#help-general').isPresent()).toBeTruthy();
    expect($('li#help-template').isPresent()).toBeTruthy();
    expect($('li#help-controller').isPresent()).toBeTruthy();
    $('.modal-header .close').click();
    browser.waitForAngular();
    expect($('.modal-header').isPresent()).toBeFalsy();
  });

  it('should not open a confirm dialog if there is no modification', function() {
    $('.EditorHeader-homeNav').click();
    expect(browser.getCurrentUrl()).toMatch(/\/home/);
  });

  it('should open a confirm dialog before going home', function() {
    element.all(by.model('widget.description')).sendKeys('update');
    $('.EditorHeader-homeNav').click();

    var dialog = switchToAlert();
    expect(dialog.accept).toBeDefined();
    dialog.accept();
    expect(browser.getCurrentUrl()).toMatch(/\/home/);
  });

  it('should open a confirm dialog and stay on the same page if dismiss', function() {
    element.all(by.model('widget.description')).sendKeys('update');
    $('.EditorHeader-homeNav').click();

    var dialog = switchToAlert();
    expect(dialog.dismiss).toBeDefined();
    dialog.dismiss();
    expect(browser.getCurrentUrl()).toMatch(/\/customAwesomeWidget/);
  });

  it('should display custom widget properties', function() {
    var properties = getPropertyNamesInList();
    expect(properties).toEqual(['qualifier', 'verb']);
  });

  it('if custom only, should allow to add a property', function() {
    $('#create').click();

    $('#name').sendKeys('newProperty');
    $('#label').sendKeys('new property');
    $('#help').sendKeys('Tooltip for new property');
    $('#caption').sendKeys('Caption for new property');
    $('#default').sendKeys('Default value');

    $('button[type="submit"]').click();

    var properties = getPropertyNamesInList();
    expect(properties).toContain('newProperty');

    var property = element.all(by.repeater('property in widget.properties')).last();
    var editButton = property.element(by.css('i.fa-pencil'));
    editButton.click();

    expect($('.modal-body #name').getAttribute('value')).toBe('newProperty');
    expect($('.modal-body #label').getAttribute('value')).toBe('new property');
    expect($('.modal-body #help').getAttribute('value')).toBe('Tooltip for new property');
    expect($('.modal-body #caption').getAttribute('value')).toBe('Caption for new property');
    expect($('.modal-body #default').getAttribute('value')).toBe('Default value');

    // Standard widget: create button should be disabled
    widgetEditor = WidgetEditor.get('pbInput');
    expect(element(by.css('#create:disabled')).isPresent()).toBeTruthy();
  });

  it('should allow to update a property', function() {
    var property = element.all(by.repeater('property in widget.properties')).first();
    var editButton = property.element(by.css('i.fa-pencil'));
    editButton.click();

    var oldParamName = $('#name').getAttribute('value');
    $('#name').clear().sendKeys('updatedProperty');
    $('#label').clear().sendKeys('updated property');

    // change bond to 'Dynamic value'
    selectBond('Dynamic value');
    selectType('choice');
    $('#choices').sendKeys('red, green, blue');
    element(by.cssContainingText('#default option', 'red')).click();

    $('button[type="submit"]').click();
    var properties = getPropertyNamesInList();
    expect(properties).not.toContain(oldParamName);
    expect(properties).toContain('updatedProperty');

    var updated = element.all(by.repeater('property in widget.properties')).first();
    expect(updated.all(by.tagName('div')).get(0).getText()).toBe('updatedProperty');
    expect(updated.all(by.tagName('div')).get(1).getText()).toBe('Label: updated property');
    expect(updated.all(by.tagName('div')).get(2).getText()).toBe('Treat as Dynamic value');
    expect(updated.all(by.tagName('div')).get(3).getText()).toBe('Type: choice');
    expect(updated.all(by.tagName('div')).get(4).getText()).toBe('Choices: red, green, blue');
    expect(updated.all(by.tagName('div')).get(5).getText()).toBe('Default value: red');

    // Standard widget: update property should be disabled
    widgetEditor = WidgetEditor.get('pbInput');
    property = element.all(by.repeater('property in widget.properties')).first();
    expect(property.element(by.css('#editProperty:disabled')).isPresent()).toBeTruthy();
  });

  it('should allow to choose property type only with some bond type while creating/updating a property', function() {
    $('#create').click();

    selectBond('Constant');
    expect($('#type').isPresent()).toBeTruthy();
    expect($('#default').isPresent()).toBeTruthy();

    selectBond('Dynamic value');
    expect($('#type').isPresent()).toBeTruthy();
    expect($('#default').isPresent()).toBeTruthy();

    selectBond('Interpolation');
    expect($('#type').isPresent()).toBeFalsy();
    expect($('#default').isPresent()).toBeTruthy();

    selectBond('Bidirectional bond');
    expect($('#type').isPresent()).toBeFalsy();
    expect($('#default').isPresent()).toBeFalsy();
  });

  it('should allow to enter choices while choosing choice type', function() {
    $('#create').click();

    selectBond('Constant');
    selectType('choice');
    expect($('#choices').isPresent()).toBeTruthy();

    // choices should still be there when changing to 'Dynamic value'
    $('#choices').sendKeys('red, blue, green');
    selectBond('Dynamic value');
    expect($('#choices').isPresent()).toBeTruthy();
    expect($('#choices').getAttribute('value')).toEqual('red, blue, green');

    // not now
    selectBond('Interpolation');
    expect($('#choices').isPresent()).toBeFalsy();
  });

  it('should allow to delete a property', function() {
    var firstParam = element.all(by.repeater('property in widget.properties')).first();
    var firstParamName = firstParam.element(by.css('.PropertyName')).getText();

    firstParam.element(by.css('i.fa-trash')).click();

    var properties = getPropertyNamesInList();
    expect(properties).not.toContain(firstParamName);

    // Standard widget: delete property should be disabled
    widgetEditor = WidgetEditor.get('pbInput');
    var property = element.all(by.repeater('property in widget.properties')).first();
    expect(property.element(by.css('#deleteProperty:disabled')).isPresent()).toBeTruthy();
  });

  it('should allow to edit a widget template and controller', function() {
    // change template
    clearAndFillAceEditor('template', '<div ng-click="sayHello()">My {{ properties.qualifier }} widget just {{ properties.verb }}!</div>');

    // change controller
    clearAndFillAceEditor('controller', '$scope.sayHello = function(){ $scope.property.verb = \'saying hello\' };');

    expect($('.EditorHeader-saveIndicator .SaveIndicator').isDisplayed()).toBeFalsy();
    // save it
    $('#save').click();
    expect($('#save').isEnabled()).toBeFalsy();

    // should go back to root when saved
    expect(browser.getCurrentUrl()).toMatch(/.*#\//);

    // Standard widget: edit template and controller should be disabled
    widgetEditor = WidgetEditor.get('pbInput');
    expect(element(by.css('#template:read-only')).isPresent()).toBeTruthy();
    expect(element(by.css('#controller:read-only')).isPresent()).toBeTruthy();
  });

  it('should save a widget as', function() {
    $('button.dropdown-toggle').click();
    $('#saveAs').click();

    var newName = $('.modal-body input[name="name"]');
    var submitButton = $('.modal-footer .btn-primary');

    // input should be filled with current widget name
    expect(newName.getAttribute('value')).toBe('awesomeWidget');

    // button disabled when we enter a wrong widget name
    newName.clear();
    newName.sendKeys('wrong widget name');
    expect(submitButton.isEnabled()).toBeFalsy();

    // page is renamed and url is updated
    newName.clear();
    newName.sendKeys('GoodNewName');
    submitButton.click();
    expect(browser.getCurrentUrl()).toMatch(/.*\/widget\/customGoodNewName/); // http://host:port/#/en/widget/GoodNewName
  });

  it('should discard save as window on browser navigation', function() {
    $('button.dropdown-toggle').click();
    $('#saveAs').click();

    browser.setLocation('/');

    expect($('.modal-footer').isPresent()).toBeFalsy();
  });

  it('should display action buttons for assets', function () {
    let assets = widgetEditor.assets().list();

    // external js asset should have edit/delete
    let externalAsset = assets.first();
    expect(externalAsset.element(by.css('button i.fa-alias-import')).isPresent()).toBeFalsy();
    expect(externalAsset.element(by.css('button i.fa-search')).isPresent()).toBeFalsy();
    expect(externalAsset.element(by.css('button i.fa-pencil')).isPresent()).toBeTruthy();
    expect(externalAsset.element(by.css('button i.fa-trash')).isPresent()).toBeTruthy();

    // Non editable asset should have downnload/view/delete
    let nonEditableAsset = assets.get(1);
    expect(nonEditableAsset.element(by.css('button i.fa-alias-import')).isPresent()).toBeTruthy();
    expect(nonEditableAsset.element(by.css('button i.fa-search')).isPresent()).toBeTruthy();
    expect(nonEditableAsset.element(by.css('button i.fa-pencil')).isPresent()).toBeFalsy();
    expect(nonEditableAsset.element(by.css('button i.fa-trash')).isPresent()).toBeTruthy();

    // local editable asset  should have download/edit/delete
    let localEditableAsset = assets.get(2);
    expect(localEditableAsset.element(by.css('button i.fa-alias-import')).isPresent()).toBeTruthy();
    expect(localEditableAsset.element(by.css('button i.fa-search')).isPresent()).toBeFalsy();
    expect(localEditableAsset.element(by.css('button i.fa-pencil')).isPresent()).toBeTruthy();
    expect(localEditableAsset.element(by.css('button i.fa-trash')).isPresent()).toBeTruthy();

    // Standard widget: edit/delete should be disabled
    widgetEditor = WidgetEditor.get('pbDatePicker');
    assets = widgetEditor.assets().list();
    let firstAsset = assets.first();
    expect(firstAsset.element(by.css('#editAsset')).isEnabled()).toBeFalsy();
    expect(firstAsset.element(by.css('#deleteAsset')).isEnabled()).toBeFalsy();
  });

  it('should allow to edit a local asset', function () {
    let assets = widgetEditor.assets();

    // should display asset file content
    let popup = assets.editAsset('CSS', 'myStyle.css');
    let someRule = '.somecssrule {\n  color: blue\n}';
    expect(popup.fileContent).toBe(someRule);

    // should update content
    let someNewRule = '.somenewrule {color: red}';
    // Warning: set fileContent add content (instead of overriding it) when the editor has the focus
    popup.fileContent = someNewRule;
    popup.save();
    expect(popup.isOpen()).toBeTruthy();
    popup.dismissBtn.click();
    assets.editAsset('CSS', 'myStyle.css');
    // Note: for some reasons, a ';' is added by selenium when a css rule is added
    let expectedContent = someNewRule.concat(';', someRule);
    expect(popup.fileContent).toBe(expectedContent);

    // should not update file content while clicking on cancel
    popup.fileContent = 'Again some fresh content';
    popup.dismissBtn.click();
    expect(popup.isOpen()).toBeFalsy();
    assets.editAsset('CSS', 'myStyle.css');
    expect(popup.fileContent).toBe(expectedContent);
  });
});
