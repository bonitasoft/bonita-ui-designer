describe('alerts', function() {
  var alerts, $timeout;

  beforeEach(module('pb.common.services'));
  beforeEach(inject(function(_alerts_, _$timeout_) {
    alerts = _alerts_;
    $timeout = _$timeout_;
  }));

  it('should add and remove alerts', function() {
    expect(alerts.alerts.length).toBe(0);

    alerts.addError({ message: 'hello' });

    expect(alerts.alerts.length).toBe(1);
    expect(alerts.alerts[0].type).toBe('danger');
    expect(alerts.alerts[0].message).toBe('hello');

    alerts.remove(0);

    expect(alerts.alerts.length).toBe(0);
  });

  it('should add success alerts and remove it after a few seconds', function() {

    alerts.addSuccess('Well done');

    expect(alerts.alerts.length).toBe(1);
    expect(alerts.alerts[0].type).toBe('success');
    expect(alerts.alerts[0].message).toBe('Well done');

    $timeout.flush();

    expect(alerts.alerts.length).toBe(0);
  });

  it('should remove alerts after a few seconds', function() {
    alerts.addError('hello');

    $timeout.flush();

    expect(alerts.alerts.length).toBe(0);
  });
});
