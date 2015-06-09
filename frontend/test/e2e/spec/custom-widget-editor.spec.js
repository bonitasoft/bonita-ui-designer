describe('custom widget editor', function() {

  beforeEach(function() {
    browser.get('#/en/widget/customAwesomeWidget');

    //prevent onbeforeunload event to avoid blocking protractor when running tests
    //@see editor.page.js
    browser.executeScript('window.onbeforeunload = function(){};');
  });

  var clearAndFillAceEditor = function(elementId, text) {
    browser.actions().doubleClick($('#' + elementId + ' .ace_content')).perform();
    var area = $('#' + elementId + ' textarea');
    area.sendKeys(protractor.Key.chord(protractor.Key.CONTROL, protractor.Key.ALT, protractor.Key.SHIFT,  'd'));
    area.sendKeys(text);
  };

  function getPropertyNamesInList() {
    return element.all(by.css('.property-name')).map(function(elm) {
      return elm.getText();
    });
  }

  it('should not open a confirm dialog if there is no modification', function() {
    $('.EditorHeader-back').click();
    expect(browser.getCurrentUrl()).toMatch(/\/home/);
  });

  it('should open a confirm dialog before going home', function() {
    element.all(by.model('widget.description')).sendKeys('update');
    $('.EditorHeader-back').click();

    var dialog = browser.switchTo().alert();
    expect(dialog.accept).toBeDefined();
    dialog.accept();
    expect(browser.getCurrentUrl()).toMatch(/\/home/);
  });

  it('should open a confirm dialog and stay on the same page if dismiss', function() {
    element.all(by.model('widget.description')).sendKeys('update');
    $('.EditorHeader-back').click();

    var dialog = browser.switchTo().alert();
    expect(dialog.dismiss).toBeDefined();
    dialog.dismiss();
    expect(browser.getCurrentUrl()).toMatch(/\/customAwesomeWidget/);
  });

  it('should display custom widget properties', function() {
    var properties = getPropertyNamesInList();
    expect(properties).toEqual(['qualifier', 'verb']);
  });

  it('should allow to add a property', function() {
    $('#create').click();

    $('#name').sendKeys('newProperty');
    $('#label').sendKeys('new property');
    $('#default').sendKeys('Default value');

    $('button[type="submit"]').click();

    var properties = getPropertyNamesInList();
    expect(properties).toContain('newProperty');
  });

  it('should allow to update a property', function() {
    var editButton = element.all(by.repeater('property in widget.properties')).first().element(by.css('i.fa-pencil'));
    editButton.click();

    var oldParamName = $('#name').getAttribute('value');
    $('#name').clear();
    $('#name').sendKeys('updatedProperty');
    $('#label').clear();
    $('#label').sendKeys('updated property');
    $('#default').clear().sendKeys('Default value');

    $('button[type="submit"]').click();

    var properties = getPropertyNamesInList();
    expect(properties).not.toContain(oldParamName);
    expect(properties).toContain('updatedProperty');
  });

  it('should allow to delete a property', function() {
    var firstParam = element.all(by.repeater('property in widget.properties')).first();
    var firstParamName = firstParam.element(by.css('.property-name')).getText();

    firstParam.element(by.css('i.fa-trash')).click();

    var properties = getPropertyNamesInList();
    expect(properties).not.toContain(firstParamName);
  });

  it('should allow to edit a widget template and controller', function() {
    // change template
    clearAndFillAceEditor('template', '<div ng-click="sayHello()">My {{ properties.qualifier }} widget just {{ properties.verb }}!</div>');

    // change controller
    clearAndFillAceEditor('controller', '$scope.sayHello = function(){ $scope.property.verb = \'saying hello\' };');

    // save it
    $('#save').click();

    // should go back to root when saved
    expect(browser.getCurrentUrl()).toMatch(/.*#\//);
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
    expect(browser.getCurrentUrl()).toMatch(/.*\/widget\/customGoodNewName/);   // http://host:port/#/en/widget/GoodNewName
  });

});
