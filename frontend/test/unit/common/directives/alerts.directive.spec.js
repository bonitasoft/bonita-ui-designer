describe('alerts', function() {
  var $compile, $rootScope, element, alerts, $interval;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));

  beforeEach(inject(function(_$compile_, _$rootScope_, _alerts_, _$interval_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;
    alerts = _alerts_;
    $interval = _$interval_;

    // given an element containing the directive
    var template = '<alerts></alerts>';

    element = $compile(template)($rootScope);
  }));

  it('should display an error message', function() {
    alerts.addError('Houston, we have a problem');
    $rootScope.$digest();

    expect(element.find('div.ui-alert').hasClass('ui-alert-error')).toBeTruthy();
    expect(element.find('h4').text()).toBe('error');
    expect(element.find('p').text()).toContain('Houston, we have a problem');
  });

  it('should display an error object with html content', function() {
    alerts.addError({ title: 'there was an error', content: '<ul><li>too bad</li><li>nop</li></ul>' });
    $rootScope.$digest();

    expect(element.find('div.ui-alert').hasClass('ui-alert-error')).toBeTruthy();
    expect(element.find('h4').text()).toBe('there was an error');
    expect(element.find('p ul').html()).toBe('<li>too bad</li><li>nop</li>');
  });

  it('should display a success message', function() {
    alerts.addSuccess('Awesome, things are done');
    $rootScope.$digest();

    expect(element.find('div.ui-alert').hasClass('ui-alert-success')).toBeTruthy();
    expect(element.find('h4').text()).toBe('success');
    expect(element.find('p').text()).toContain('Awesome, things are done');
  });

  it('should display a success object with html content', function() {
    alerts.addSuccess({ title: 'You rocks', content: '<ul><li>yep</li><li>that is true</li></ul>' });
    $rootScope.$digest();

    expect(element.find('div.ui-alert').hasClass('ui-alert-success')).toBeTruthy();
    expect(element.find('h4').text()).toBe('You rocks');
    expect(element.find('p ul').html()).toBe('<li>yep</li><li>that is true</li>');
  });

  it('should display a warning message', function() {
    alerts.addWarning('Be careful, this could be dangerous');
    $rootScope.$digest();

    expect(element.find('div.ui-alert').hasClass('ui-alert-warning')).toBeTruthy();
    expect(element.find('h4').text()).toBe('warning');
    expect(element.find('p').text()).toContain('Be careful, this could be dangerous');
  });

  it('should display a warning object with html content', function() {
    alerts.addWarning({ title: 'Winter is coming', content: '<b>Keep your eyes open</b>' });
    $rootScope.$digest();

    expect(element.find('div.ui-alert').hasClass('ui-alert-warning')).toBeTruthy();
    expect(element.find('h4').text()).toBe('Winter is coming');
    expect(element.find('p b').text()).toBe('Keep your eyes open');
  });

  it('should disappear', function() {
    alerts.addError('Houston, we have a problem');
    $rootScope.$digest();

    $interval.flush(8000);

    expect(element.find('div.ui-alert').length).toBe(0);
  });

  it('should be closable', function() {
    alerts.addError('Houston, we have a problem');
    $rootScope.$digest();

    element.find('div.ui-alert.ui-alert-error button.close').click();

    expect(element.find('div.ui-alert').length).toBe(0);
  });
});
