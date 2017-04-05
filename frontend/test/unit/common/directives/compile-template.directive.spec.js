describe('compile template directive', function() {
  var $compile, element, scope;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));
  beforeEach(inject(function(_$compile_, $rootScope) {
    $compile = _$compile_;

    scope = $rootScope.$new();

    scope.template = '<div ng-if="insert">inner content</div>';
  }));

  it('should compile directives and insert content', function() {
    scope.insert = true;
    element = $compile('<div compile-template="template"></div>')(scope);
    scope.$digest();

    expect(element.find('div').length).toBe(1);
  });

  it('should compile directives and not insert content', function() {
    scope.insert = false;
    element = $compile('<div compile-template="template"></div>')(scope);
    scope.$digest();

    expect(element.find('div').length).toBe(0);
  });
});
