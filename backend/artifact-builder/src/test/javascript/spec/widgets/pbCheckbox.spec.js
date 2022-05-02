describe('pbCheckbox', function() {

  var compile, scope, element;

  beforeEach(module('bonitasoft.ui.widgets'));
  beforeEach(module('bonitasoft.ui.services'));

  beforeEach(inject(function ($injector){
    compile = $injector.get('$compile');
    scope = $injector.get('$rootScope').$new();
    scope.properties = {
      isBound: function() {
        return false;
      },
      disabled: false
    };
  }));

  beforeEach(function() {
    element = compile('<pb-checkbox></pb-checkbox>')(scope);
    scope.$apply();
  });

  it('should have a label', function() {
    scope.properties.label = 'Checkbox label';
    scope.$apply();

    expect(element.find('label').text().trim()).toBe('Checkbox label');
  });

  it('should allows html markup to be interpreted', function() {
    scope.properties = angular.extend(scope.properties, {
      label: '<span>allow html!</span>',
      allowHTML: true
    });

    var element = compile('<pb-checkbox></pb-checkbox>')(scope);
    scope.$apply();
    var label = element.find('label');
    expect(label.text().trim()).toBe('allow html!');
  });

  it('should prevent html markup to be interpreted', function() {
    scope.properties = angular.extend(scope.properties, {
      label: '<span>allow html!</span>',
      allowHTML: false
    });

    var element = compile('<pb-checkbox></pb-checkbox>')(scope);
    scope.$apply();
    var label = element.find('label');
    expect(label.text().trim()).toBe('<span>allow html!</span>');
  });

  it('should be disabled  when requested', function() {
    scope.properties.disabled = true;
    scope.$apply();

    expect(element.find('input').attr('disabled')).toBe('disabled');
  });

  it('should be required  when requested', function() {
    scope.properties.required = true;
    scope.$apply();

    expect(element.find('input').attr('required')).toBe('required');
  });

  it('should be checked when value is true', function() {
    scope.properties.value = true;
    scope.$apply();

    expect(element.find('input').get(0).checked).toBe(true);
  });

  it('should be checked when value is "true"', function() {
    scope.properties.value = 'true';
    scope.$apply();

    expect(element.find('input').get(0).checked).toBe(true);
  });

  it('should not be checked otherwise', function() {
    scope.properties.value = undefined;
    scope.$apply();

    expect(element.find('input').get(0).checked).toBe(false);

    scope.properties.value = '';
    scope.$apply();

    expect(element.find('input').get(0).checked).toBe(false);

    scope.properties.value = { it: "should not be checked"};
    scope.$apply();

    expect(element.find('input').get(0).checked).toBe(false);
  });


});
