describe('Service: urlParameterResolver', function() {

  beforeEach(module('bonitasoft.ui.services'));

  let model, barValue, $rootScope;

  beforeEach(inject((ResolverService, $location, _$rootScope_) => {
    $rootScope = _$rootScope_;
    model = {};
    barValue = 'baz';
    spyOn($location, 'absUrl').and.callFake(() => `/path#?bar=${barValue}`);
    let resolver = ResolverService.createResolver(model, 'foo', {
      type: 'urlparameter',
      value: 'bar'
    });
    resolver.resolve();
    resolver.watchDependencies();
  }));

  it('should retrieve a parameter from the url', () => {
    expect(model.foo).toBe('baz');
  });

  it('should update model value when url change', () => {
    barValue = 'qux';
    $rootScope.$apply();

    expect(model.foo).toBe('qux');
  });
});
