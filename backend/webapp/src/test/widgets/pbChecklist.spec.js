describe('pbChecklist', function() {

  var compile, scope, dom;

  beforeEach(module('org.bonitasoft.pagebuilder.widgets'));

  beforeEach(inject(function ($injector){
    compile = $injector.get('$compile');
    scope = $injector.get('$rootScope').$new();
    scope.properties = {
      inline: false,
      disabled: false,
      availableValues: ['jeanne', 'serge', 'maurice']
    };
  }));

  beforeEach(function() {
    dom = compile('<pb-checklist></pb-checklist>')(scope);
    scope.$apply();
  });

  it('should contain 3 inputs inside a div', function() {
    expect(dom.find('input').length).toBe(3);
  });

  it('should not display inlined checkbox', function() {
    expect(dom.find('label').hasClass('checkbox-inline')).toBe(false);
  });

  it('should display the textOption', function() {
    expect(dom.find('label').eq(0).text().trim()).toBe('jeanne');
    expect(dom.find('label').eq(1).text().trim()).toBe('serge');
    expect(dom.find('label').eq(2).text().trim()).toBe('maurice');
  });

  it('should display inlined checkbox', function() {
    scope.properties.inline = true;
    scope.$apply();
    expect(dom.find('label').hasClass('checkbox-inline')).toBe(true);
  });

  it('should format the label if a label is provided', function() {

    scope.properties.displayedKey = 'name';
    scope.properties.availableValues =  [
      {name: 'serge', id: 1},
      {name: 'jeanne', id: 2}
    ];
    var widget = compile('<pb-checklist></pb-checklist>')(scope);
    scope.$apply();

    expect(widget.find('label').eq(0).text().trim()).toBe('serge');
    expect(widget.find('label').eq(1).text().trim()).toBe('jeanne');
  });

  it('should exposed the selectedValues', function() {

    scope.properties.displayedKey = 'name';
    scope.properties.availableValues =  [
      {name: 'serge', id: 1},
      {name: 'jeanne', id: 2},
    ];
    scope.$apply();

    dom.find('input').eq(0).click();

    expect(scope.properties.selectedValues.length).toBe(1);
    expect(scope.properties.selectedValues[0]).toEqual({name: 'serge', id: 1});
  });

  it('should allow to select a specific properties for result object', function() {
    scope.properties.displayedKey = 'name';
    scope.properties.returnedKey = 'name';
    scope.properties.availableValues =  [
      {name: 'serge', id: 1},
      {name: 'jeanne', id: 2}
    ];
    var widget = compile('<pb-checklist></pb-checklist>')(scope);
    scope.$apply();

    dom.find('input').eq(0).click();

    expect(scope.properties.selectedValues.length).toBe(1);
    expect(scope.properties.selectedValues[0]).toBe('serge');
  });

  it('should preselect the selectedValues', function() {

    scope.properties.displayedKey = 'name';
    scope.properties.availableValues =  [
      {name: 'serge', id: 1},
      {name: 'jeanne', id: 2},
    ];
    scope.properties.selectedValues =  [
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
});
