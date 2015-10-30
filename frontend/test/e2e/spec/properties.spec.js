var PageEditor = require('../pages/editor.page.js');

describe('properties panel test', function() {

  var editor;
  beforeEach(function() {
    editor = PageEditor.get('empty');
    editor.addWidget('pbInput');
    editor.setWidth(6);

  });

  it('should display default properties on container', function() {
    editor.addWidget('container');
    editor.setWidth(12);
    expect(element(by.cssContainingText('.property-label label', 'CSS classes')).isPresent()).toBe(true);
    expect(element(by.cssContainingText('.property-label label', 'Hidden')).isPresent()).toBe(true);
  });

  it('should display properties for label', function() {
    expect(element(by.cssContainingText('.property-label label', 'CSS classes')).isPresent()).toBe(true);
    expect(element(by.cssContainingText('.property-label label', 'Hidden')).isPresent()).toBe(true);
    expect(element(by.cssContainingText('.property-label label', 'Label')).isPresent()).toBe(true);
    expect(element(by.cssContainingText('.property-label label', 'Label position')).isPresent()).toBe(true);
    expect(element(by.cssContainingText('.property-label label', 'Label width')).isPresent()).toBe(true);
    expect(element(by.cssContainingText('.property-label label', 'Placeholder')).isPresent()).toBe(true);
    expect(element(by.cssContainingText('.property-label label', 'Value')).isPresent()).toBe(true);
    expect(element(by.cssContainingText('.property-label label', 'Type')).isPresent()).toBe(true);
  });

  it('should hide properties for label if we choose to hide it', function() {

    // Change the value of a radio button as Angular does not understand when we change prop :/
    var labelHidden = $$('[data-property=labelHidden] input[type=radio]').last();
    var propertyLabel = $$('property-field .property-label');

    expect(propertyLabel.count()).toBe(13);
    labelHidden.click();
    expect(propertyLabel.count()).toBe(10);
  });

  it('should add a component set a type then add a new one and get default config', function() {
    // Select option number
    element(by.css('[data-property=type] option[label="number"]')).click();

    editor.addWidget('pbInput');

    var type = element(by.css('[data-property=type] select'));
    expect(type.$('option:checked').getText()).toBe('text');
  });

  it('should reload the config for properties', function() {
    // Select option number
    element(by.css('[data-property=type] option[label="number"]')).click();

    editor.addWidget('pbInput');

    var type = element(by.css('[data-property=type] select'));
    expect(type.$('option:checked').getText()).toBe('text');

    // Click on the first widget
    $('#component-0').click();

    type = element(by.css('[data-property=type] select'));
    expect(type.$('option:checked').getText()).toBe('number');
  });

});
