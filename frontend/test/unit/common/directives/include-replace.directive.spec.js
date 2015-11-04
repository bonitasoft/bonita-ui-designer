describe('directive includeReplace', function() {

  var compile, scope, templateCache;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));

  beforeEach(inject(function($injector, $rootScope) {
    templateCache = $injector.get('$templateCache');
    compile = $injector.get('$compile');
    scope = $rootScope.$new();
  }));

  it('should replace ng-include tag with template', function() {
    templateCache.put('template.html', '<p>hello world</p>');

    var element = compile('<div><ng-include src="\'template.html\'" include-replace></ng-include></div>')(scope);
    scope.$digest();

    expect(element.find('ng-include').length).toBe(0);
  });
});
