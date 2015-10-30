describe('importReportItemList directive', () => {
  var scope;
  beforeEach(angular.mock.module('bonitasoft.designer.home.import'));
  beforeEach(inject(($rootScope, $compile) => {
    scope = $rootScope.$new();
    var markup = '<import-report-item-list page-name="element.name" type="type" display-page="!overridden" dependencies="dependencies.added"></import-report-item-list> ';
    var element = $compile(markup)(scope);
    scope.$apply();
    scope = element.isolateScope();
  }));
  it('should join artifact names',() => {
    expect(scope.joinOnNames()).toBeUndefined();
    expect(scope.joinOnNames([])).toEqual('');
    var artifacts = [{ name: 'widget1' }, { name: 'widget3' }];
    expect(scope.joinOnNames(artifacts)).toEqual('widget1, widget3');
  });

});
