describe('alerts', function () {
  var $compile, $rootScope, element;

  beforeEach(module('pb.common.directives'));

  beforeEach(inject(function (_$compile_, _$rootScope_, alerts) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;
    $rootScope.alerts = alerts;

    // given an element containing the directive
    var template = '<alerts></alerts>';

    element = $compile(template)($rootScope);

    alerts.addError({message: 'Houston, we have a problem'});
    $rootScope.$digest();
  }));

  it('should display an alert', function () {
    expect(element.text()).toContain('Houston, we have a problem');
  });

  it('should close the alert', function () {
    expect(element.find('button').length).toBe(1);
    element.find('button').click();
    expect(element.find('button').length).toBe(0);
    expect(element.text()).not.toContain('Houston, we have a problem');
  });
});
