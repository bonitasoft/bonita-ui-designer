describe('pbCheckbox', function() {

  var compile, scope, element;

  beforeEach(module('org.bonitasoft.pagebuilder.widgets'));

  beforeEach(inject(function ($injector){
    compile = $injector.get('$compile');
    scope = $injector.get('$rootScope').$new();
    scope.properties = {
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

  it('should be disabled  when requested', function() {
    scope.properties.disabled = true;
    scope.$apply();

    expect(element.find('input').attr('disabled')).toBe('disabled');
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
