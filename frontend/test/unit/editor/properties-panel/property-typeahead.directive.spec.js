describe('property typeahead', () => {

  let element, scope, $timeout;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.properties-panel'));

  beforeEach(inject(function(_$compile_, $rootScope, _$timeout_) {
    let $compile = _$compile_;
    $timeout = _$timeout_;

    scope = $rootScope.$new();
    scope.model = undefined;
    scope.data = ['Alabama', 'Oregon'];

    element = $compile(`
        <div>
          <uid-property-typeahead 
              uid-property-typeahead-values="data" 
              uid-property-typeahead-model="model"> 
          </uid-property-typeahead>
        </div>`)(scope);
    scope.$apply();
  }));

  function getSuggestions(element) {
    let lis = element[0].querySelectorAll('li');
    return Object.entries(lis)
      .map(entry => angular.element(entry[1]).text().trim())
      .filter(text => text);
  }

  it('should display suggestions based on user typing', () => {
    element.find('input').val('Or').trigger('input');

    expect(getSuggestions(element)).toEqual(['Oregon']);
  });

  it('should display suggestions based on user typing and be case insensitive', () => {
    element.find('input').val('or').trigger('input');

    expect(getSuggestions(element)).toEqual(['Oregon']);
  });

  it('should display suggestions when user type a !', () => {
    element.find('input').val('!').trigger('input');

    expect(getSuggestions(element)).toEqual(['Alabama', 'Oregon']);
  });

  it('should display suggestions on focus', () => {
    element.find('input').triggerHandler('focus');
    // internal ui-bootstrap typeahead might be based on $timeout. need to flush it to get suggestions on focus
    $timeout.flush();

    expect(getSuggestions(element)).toEqual(['Alabama', 'Oregon']);
  });

  it('should set the model when user type anything that is not in suggestions', () => {
    element.find('input').val('notInsuggestions').trigger('input');

    expect(scope.model).toBe('notInsuggestions');
  });

  it('should set the model when user type a ! and anything that is not in suggestions', () => {
    element.find('input').val('!notInsuggestions').trigger('input');

    expect(scope.model).toBe('!notInsuggestions');
  });

  it('should set the model when user type something that is in suggestions', () => {
    element.find('input').val('Oregon').trigger('input');

    expect(scope.model).toBe('Oregon');
  });

  it('should set the model when user type a ! and something that is in suggestions', () => {
      element.find('input').val('!Alabama').trigger('input');

      expect(scope.model).toBe('!Alabama');
  });

  it('should set the model when user select an entry from suggestions', () => {
    element.find('input').val('Al').trigger('input');

    element.find('li > a').click();

    expect(scope.model).toBe('Alabama');
  });

  it('should set the model when user type a ! and select an entry from suggestions', () => {
    element.find('input').val('!or').trigger('input');

    element.find('li > a').click();

    expect(scope.model).toBe('!Oregon');
  });
});
