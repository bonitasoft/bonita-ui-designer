describe('validJSon', function() {
  var $compile, $rootScope, element;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.data-panel'));
  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;

    // given an element containing the directive
    var template = '<input ng-model="data" valid-json>';
    element = $compile(template)($rootScope);
  }));

  it('should be valid if empty', function() {
    // then we should have
    expect(element.attr('class')).not.toContain('ng-invalid');
  });

  it('should be valid if containing valid Json', function() {
    $rootScope.data = '[]';
    $rootScope.$digest();
    expect(element.attr('class')).not.toContain('ng-invalid');

    $rootScope.data = '["rete"]';
    $rootScope.$digest();
    expect(element.attr('class')).not.toContain('ng-invalid');

    $rootScope.data = '{}';
    $rootScope.$digest();
    expect(element.attr('class')).not.toContain('ng-invalid');

    $rootScope.data = '{"key": "value"}';
    $rootScope.$digest();
    expect(element.attr('class')).not.toContain('ng-invalid');
  });

  it('should be invalid if containing bad formated json', function() {
    $rootScope.data = '[{dqs; ';
    $rootScope.$digest();
    expect(element.attr('class')).toContain('ng-invalid');
  });

});
