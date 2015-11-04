describe('alerts', function() {
  var alerts, $interval;

  beforeEach(angular.mock.module('bonitasoft.designer.common.services'));
  beforeEach(inject(function(_alerts_, _$interval_) {
    alerts = _alerts_;
    $interval = _$interval_;
  }));

  describe('success', function() {

    it('can be added as message', function() {
      alerts.addSuccess('hello');

      expect(alerts.alerts.length).toBe(1);
      expect(alerts.alerts[0].type).toBe('success');
      expect(alerts.alerts[0].content).toBe('hello');
    });

    it('can be added as object', function() {
      alerts.addSuccess({ title: 'a title', content: 'a content' });

      expect(alerts.alerts.length).toBe(1);
      expect(alerts.alerts[0].type).toBe('success');
      expect(alerts.alerts[0].content).toBe('a content');
      expect(alerts.alerts[0].title).toBe('a title');
    });

    it('can be removed', function() {
      alerts.addSuccess({ title: 'a title', content: 'a content' });

      alerts.remove(0);

      expect(alerts.alerts.length).toBe(0);
    });

    it('should be removed after a 8 seconds', function() {
      alerts.addSuccess('Well done');

      $interval.flush(8000);

      expect(alerts.alerts.length).toBe(0);
    });

  });

  describe('error', function() {

    it('can be added as message', function() {
      alerts.addError('hello');

      expect(alerts.alerts.length).toBe(1);
      expect(alerts.alerts[0].type).toBe('error');
      expect(alerts.alerts[0].content).toBe('hello');
    });

    it('can be added as object', function() {
      alerts.addError({ title: 'a title', content: 'a content' });

      expect(alerts.alerts.length).toBe(1);
      expect(alerts.alerts[0].type).toBe('error');
      expect(alerts.alerts[0].content).toBe('a content');
      expect(alerts.alerts[0].title).toBe('a title');
    });

    it('can be removed', function() {
      alerts.addError({ title: 'a title', content: 'a content' });

      alerts.remove(0);

      expect(alerts.alerts.length).toBe(0);
    });

    it('should be removed after 8 seconds', function() {
      alerts.addError('Well done');

      $interval.flush(8000);

      expect(alerts.alerts.length).toBe(0);
    });

  });

  describe('warning', function() {

    it('can be added as message', function() {
      alerts.addWarning('hello');

      expect(alerts.alerts.length).toBe(1);
      expect(alerts.alerts[0].type).toBe('warning');
      expect(alerts.alerts[0].content).toBe('hello');
    });

    it('can be added as object', function() {
      alerts.addWarning({ title: 'a title', content: 'a content' });

      expect(alerts.alerts.length).toBe(1);
      expect(alerts.alerts[0].type).toBe('warning');
      expect(alerts.alerts[0].content).toBe('a content');
      expect(alerts.alerts[0].title).toBe('a title');
    });

    it('can be removed', function() {
      alerts.addWarning({ title: 'a title', content: 'a content' });

      alerts.remove(0);

      expect(alerts.alerts.length).toBe(0);
    });

    it('should be removed after a few seconds', function() {
      alerts.addWarning('Well done');

      $interval.flush(8000);

      expect(alerts.alerts.length).toBe(0);
    });

  });
});
