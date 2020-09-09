describe('fragment data binding field directive', function() {
  var $compile, element, template, scope, directiveScope;

  beforeEach(angular.mock.module('bonitasoft.designer.templates', 'bonitasoft.designer.editor.properties-panel'));
  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    var $rootScope = _$rootScope_;

    scope = $rootScope.$new();
    scope.fragmentDataName = 'data1';
    scope.pageData = { pageData66: { type: 'constant', value: null }, pageData77: { type: 'constant', value: null } };

    template = '<fragment-data-binding-field data-name="fragmentDataName" binding="binding" page-data="pageData" />';
    element = $compile(template)(scope);
    scope.$apply();

    directiveScope = element.isolateScope();
  }));

  it('should display fragment data label', function() {
    expect(element.find('label').text()).toBe('data1');
  });

  it('should display bound page data and a linked icon', function() {
    scope.binding = 'pageData77';
    scope.$apply();

    expect(element.find('input').val()).toBe('pageData77');
    expect(element.find('i.fa-link').length).toBe(1);
  });

  it('should display a unlinked icon when not bound', function() {
    scope.binding = '';
    scope.$apply();

    expect(element.find('input').val()).toBe('');
    expect(element.find('i.fa-unlink').length).toBe(1);
  });

});
