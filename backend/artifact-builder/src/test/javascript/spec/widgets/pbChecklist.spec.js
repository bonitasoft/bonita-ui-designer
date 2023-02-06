
describe('pbChecklist', function () {

  var compile, scope, dom;

  beforeEach(module('bonitasoft.ui.widgets'));
  beforeEach(module('bonitasoft.ui.services'));

  beforeEach(inject(function ($injector) {
    compile = $injector.get('$compile');
    scope = $injector.get('$rootScope').$new();
    scope.properties = {
      isBound: function() {
        return false;
      },
      inline: false,
      disabled: false,
      label: 'foobar',
      availableValues: ['jeanne', 'serge', 'maurice']
    };
  }));

  beforeEach(function () {
    dom = compile('<pb-checklist></pb-checklist>')(scope);
    scope.$apply();
  });

  it('should contain 3 inputs inside a div', function () {
    expect(dom.find('input').length).toBe(3);
  });

  it('should not display inlined checkbox', function () {
    expect(dom.find('label').hasClass('checkbox-inline')).toBe(false);
  });

  it('should display the textOption', function () {
    expect(dom.find('div.checkbox label').eq(0).text().trim()).toBe('jeanne');
    expect(dom.find('div.checkbox label').eq(1).text().trim()).toBe('serge');
    expect(dom.find('div.checkbox label').eq(2).text().trim()).toBe('maurice');
  });

  it('should display inlined checkbox', function () {
    scope.properties.inline = true;
    scope.$apply();
    expect(dom.find('label').hasClass('checkbox-inline')).toBe(true);
  });

  it('should format the label if a label is provided', function () {

    scope.properties.displayedKey = 'name';
    scope.properties.availableValues = [
      {name: 'serge', id: 1},
      {name: 'jeanne', id: 2}
    ];
    var widget = compile('<pb-checklist></pb-checklist>')(scope);
    scope.$apply();

    expect(widget.find('div.checkbox label').eq(0).text().trim()).toBe('serge');
    expect(widget.find('div.checkbox label').eq(1).text().trim()).toBe('jeanne');
  });

  it('should exposed the selectedValues', function () {

    scope.properties.displayedKey = 'name';
    scope.properties.availableValues = [
      {name: 'serge', id: 1},
      {name: 'jeanne', id: 2}
    ];
    scope.$apply();

    dom.find('input').eq(0).click();

    expect(scope.properties.selectedValues.length).toBe(1);
    expect(scope.properties.selectedValues[0]).toEqual({name: 'serge', id: 1});
  });

  it('should allow to select a specific properties for result object', function () {
    scope.properties.displayedKey = 'name';
    scope.properties.returnedKey = 'name';
    scope.properties.availableValues = [
      {name: 'serge', id: 1},
      {name: 'jeanne', id: 2}
    ];
    compile('<pb-checklist></pb-checklist>')(scope);
    scope.$apply();

    dom.find('input').eq(0).click();

    expect(scope.properties.selectedValues.length).toBe(1);
    expect(scope.properties.selectedValues[0]).toBe('serge');
  });

  it('should select items whenever the selectedValues changes', function () {

    scope.properties.displayedKey = 'name';
    scope.properties.availableValues = [
      {name: 'serge', id: 1},
      {name: 'jeanne', id: 2}
    ];
    scope.$apply();

    scope.properties.selectedValues = [
      {name: 'serge', id: 1}
    ];
    scope.$apply();


    expect(dom.find('input').get(0).checked).toBe(true);
  });

  it('should be disabled when requested', function () {
    scope.properties.disabled = true;

    var element = compile('<pb-checklist></pb-checklist>')(scope);
    scope.$apply();

    expect(element.find('input').attr('disabled')).toBe('disabled');
  });

  describe('label', function () {

    it('should be on top by default if displayed', function () {
      scope.properties.label = 'foobar';

      var element = compile('<pb-checklist></pb-checklist>')(scope);
      scope.$apply();

      expect(element.find('div.form-group').hasClass('form-horizontal')).toBeFalsy();
      expect(element.find('legend.control-label').text().trim()).toBe('foobar');
    });

    it('should be on the left of the checklist', function () {
      scope.properties = angular.extend(scope.properties, {
        label: 'barbaz',
        labelPosition: 'left'
      });

      var element = compile('<pb-checklist></pb-checklist>')(scope);
      scope.$apply();

      expect(element.find('div.form-group').hasClass('form-horizontal')).toBeTruthy();
      expect(element.find('legend.control-label').text().trim()).toBe('barbaz');
    });

    it('should not be there when labelHidden is truthy', function () {
      scope.properties.labelHidden = true;

      var element = compile('<pb-checklist></pb-checklist>')(scope);
      scope.$apply();

      expect(element.find('legend.control-label').length).toBe(0);
    });

    it('should allows html markup to be interpreted', function() {
      scope.properties = angular.extend(scope.properties, {
        label: '<span>allow html!</span>',
        allowHTML: true
      });

      var element = compile('<pb-checklist></pb-checklist>')(scope);
      scope.$apply();
      var label = element.find('legend.control-label');
      expect(label.text().trim()).toBe('allow html!');
    });

    it('should prevent html markup to be interpreted', function() {
      scope.properties = angular.extend(scope.properties, {
        label: '<span>allow html!</span>',
        allowHTML: false
      });

      var element = compile('<pb-checklist></pb-checklist>')(scope);
      scope.$apply();
      var label = element.find('legend.control-label');
      expect(label.text().trim()).toBe('<span>allow html!</span>');
    });

  });
});
