describe('alert-content directive', function() {

  var element, scope, templateCache;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));

  beforeEach(inject(function($compile, $rootScope, $templateCache) {
    templateCache = $templateCache;
    scope = $rootScope.$new();
    scope.alert = {};

    var template = '<p alert-content="alert"></p>';
    element = $compile(template)(scope);
    scope.$apply();
  }));

  it('should display alert html content', function() {
    scope.alert = {
      content: '<h1>this is some html code</h1>'
    };
    scope.$apply();

    expect(element.find('h1').text()).toBe('this is some html code');
  });

  it('should display alert html content from template url', function() {
    templateCache.put('js/template/testTemplate', '<h1>this is some html code {{ dude }}</h1>');
    scope.alert = {
      contentUrl: 'js/template/testTemplate',
      context: { dude: 'Colin' }
    };
    scope.$apply();

    expect(element.find('h1').text()).toBe('this is some html code Colin');
  });
});
