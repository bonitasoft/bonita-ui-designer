describe('pbCheckbox', function() {

  var compile, rootScope, dom, scope;

  beforeEach(module('org.bonitasoft.pagebuilder.widgets'));
  beforeEach(module('org.bonitasoft.pagebuilder.generator.services'));

  beforeEach(inject(function ($injector){
    compile = $injector.get('$compile');
    rootScope = $injector.get('$rootScope');
    scope = rootScope.$new();
    scope.properties = {
      inline: false,
      availableValues: ['jeanne', 'serge', 'maurice'],
      selectedValue: 'serge',
      disabled: false
    };
  }));

  beforeEach(function() {
    dom = compile('<pb-radio-box></pb-radio-box>')(scope);
    scope.$apply();
  });

  it('should contain an input radio inside a div', function() {
    expect(dom.find('input').length).toBe(3);
  });

  it('should not display inlined checkbox', function() {
    expect(dom.find('label').hasClass('radio-inline')).toBe(false);
  });

  it('should display the textOption', function() {
    expect(dom.find('label').eq(0).text().trim()).toBe('jeanne');
    expect(dom.find('label').eq(1).text().trim()).toBe('serge');
    expect(dom.find('label').eq(2).text().trim()).toBe('maurice');
  });

  it('should display inlined checkbox', function() {
    scope.properties.inline = true;
    scope.$apply();
    expect(dom.find('label').hasClass('radio-inline')).toBe(true);
  });

  it('should select the radio in the list when selectedValue is provided', function() {
    var selectedElement = [].slice.call(dom.find("input")).filter(function(el){
      return el.checked === true;
    });

    expect(selectedElement.length).toBe(1);
    expect(selectedElement[0].parentNode.textContent.trim()).toBe('serge');
  });

  it('should display the correct label if label property  is provided', function(){
    scope.properties.availableValues = [{name: 'jeanne'}];
    scope.properties.displayedKey = 'name';
    var widget = compile('<pb-radio-box></pb-radio-box>')(scope);
    scope.$digest();
    expect(widget.find("label").eq(0).text().trim()).toBe("jeanne")
  });

  it('should display data as label if label property is not provided', function(){
    scope.properties.availableValues = [{"name": 'jeanne'}];
    scope.$digest();
    expect(dom.find("label").eq(0).text().trim()).toBe(JSON.stringify(scope.properties.availableValues[0]));
  });

  it('should select the radio  if selectedValue is  not null', function(){
    scope.properties.availableValues = [{"name": 'jeanne'}, {"name": 'serge'}];
    scope.properties.selectedValue = {"name": 'serge'};
    scope.properties.displayedKey = 'name';
    var widget = compile('<pb-radio-box></pb-radio-box>')(scope);
    scope.$digest();

    var selectedElement = [].slice.call(widget.find("input")).filter(function(el){
      return el.checked === true;
    });
    expect(selectedElement.length).toBe(1);
    expect(selectedElement[0].parentNode.textContent.trim()).toBe('serge');
  });

  it('should select the correct radio if selectedValue is not null, depending of returnedKey', function(){
    var scope = rootScope.$new();
    scope.properties = {
      availableValues: [{"name": 'jeanne'}, {"name": 'serge'}],
      selectedValue: 'serge',
      displayedKey: 'name',
      returnedKey: 'name'
    }
    var widget = compile('<pb-radio-box></pb-radio-box>')(scope);
    scope.$digest();

    var selectedElement = [].slice.call(widget.find("input")).filter(function(el){
      return el.checked === true;
    });
    expect(selectedElement.length).toBe(1);
    expect(selectedElement[0].parentNode.textContent.trim()).toBe('serge');
  });

  it('should set a value according the ', function(){
    scope.properties.availableValues = [{"name": 'jeanne'}];
    scope.$digest();
    expect(dom.find("label").eq(0).text().trim()).toBe(JSON.stringify(scope.properties.availableValues[0]));
  });


  it('should be disabled when requested', function () {
    scope.properties.disabled = true;
    var element = compile('<pb-radio></pb-radio>')(scope);
    scope.$apply();
    [].slice.call(element.find('input')[0]).forEach(function(input) {
      expect(input.attr('disabled')).toBe('disabled');
    })
  });

  it('should be required when requested', function () {
    scope.properties.required = true;
    var element = compile('<pb-radio></pb-radio>')(scope);
    scope.$apply();
    [].slice.call(element.find('input')[0]).forEach(function(input) {
      expect(input.attr('required')).toBe('required');
    })
});
});
