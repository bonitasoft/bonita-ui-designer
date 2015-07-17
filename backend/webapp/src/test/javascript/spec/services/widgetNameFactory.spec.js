describe('Service: widgetNameFactory', function () {

  beforeEach(module('bonitasoft.ui.services'));

  var service;

  beforeEach(inject(function (widgetNameFactory) {
    service = widgetNameFactory;
  }));

  it('should return a new name', function () {
    expect(service.getName('foo')).toBe('foo0');
  });

  it('should return a incremnet an already known name', function () {
    service.getName('foo')
    expect(service.getName('foo')).toBe('foo1');
  });
});
