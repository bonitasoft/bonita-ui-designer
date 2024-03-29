describe('widget property field', function() {
  var $compile, element, scope, timeout;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.properties-panel'));
  beforeEach(inject(function(_$compile_, $rootScope, $timeout) {
    $compile = _$compile_;
    timeout = $timeout;

    scope = $rootScope.$new();
    scope.propertyValue = {};

    scope.property = {
      label: 'foobar',
      type: 'boolean',
      bond: 'expression'
    };

    element = $compile('<property-field property="property" property-value="propertyValue"></property-field>')(scope);
    scope.$apply();
  }));

  it('should display a boolean expression', function() {
    expect(element.find('.property-label label').text().trim()).toBe('foobar');
    expect(element.find('input').attr('type')).toBe('radio');
    expect(element.find('button').length).toBe(1);
  });

  it('should allow switching to the expression editor when the property is of type expression', function() {
    expect(element.find('input').attr('type')).toBe('radio');
    element.find('button').click();
    spyOn(element.find('input')[0], 'focus');
    timeout.flush();

    expect(element.find('input').attr('type')).toBe('text');
    expect(element.find('input')[0].focus).toHaveBeenCalled();
    expect(scope.propertyValue.type).toBe('expression');
    element.find('button').click();

    expect(scope.propertyValue.type).toBe('constant');
    expect(element.find('input').attr('type')).toBe('radio');
  });

  it('should update propertyValue with expression content', function() {
    element.find('button').click();

    expect(element.find('input').attr('type')).toBe('text');
    element.find('input').val('hello').trigger('input');

    expect(scope.propertyValue.value).toBe('hello');
  });

  //TODO BP: Reactivate it before merge
  it('should update propertyValue with constant value', inject(function($document, $rootElement) {
    let el = element.find('.radio-inline input');
    let firstRadioButton = el.get(1);

    $rootElement.append(firstRadioButton);
    $document.find('body').append($rootElement);

    firstRadioButton.click();

    expect(scope.propertyValue.value).toBe(true);
  }));

  it('should display a constant property', function() {
    scope.property.bond = 'constant';

    expect(element.find('.property-label label').text().trim()).toBe('foobar');
    expect(element.find('.radio-inline').length).toBe(2);
  });

  it('should display a variable property', function() {
    scope.property.bond = 'variable';
    scope.$digest();

    expect(element.find('.property-label label').text().trim()).toBe('foobar');
    expect(element.find('input').attr('type')).toBe('text');
  });

  it('should display an interpolation property', function() {
    scope.property.type = 'text';
    scope.property.bond = 'interpolation';
    scope.$digest();

    expect(element.find('.property-label label').text().trim()).toBe('foobar');
    expect(element.find('input').attr('type')).toBe('text');
  });

  it('should display a textarea for interpolation html property', function() {
    scope.property.type = 'html';
    scope.property.bond = 'interpolation';
    scope.$digest();

    expect(element.find('.property-label label').text().trim()).toBe('foobar');
    expect(element.find('textarea').length).toBe(1);
  });
});
