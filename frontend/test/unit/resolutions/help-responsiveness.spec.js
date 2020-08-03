describe('UidResponsiveHelpTab', function(){
  var scope, compile;
  beforeEach(angular.mock.module('bonitasoft.designer.resolution', 'bonitasoft.designer.templates'));
  beforeEach(inject(function($rootScope, $compile) {
    scope = $rootScope.$new();
    compile = $compile;
  }));

  it('should display responiveness help if feature is activated', function() {
    var element = compile('<div><uid-responsive-help-tab></uid-responsive-help-tab></div>')(scope);
    scope.$apply();
    expect(element.find('#help-responsiveness').length).toBe(1);
  });
});

