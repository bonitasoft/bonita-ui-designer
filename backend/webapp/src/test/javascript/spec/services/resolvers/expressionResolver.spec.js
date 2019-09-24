describe('Service: expressionResolver', function () {

  beforeEach(module('bonitasoft.ui.services'));

  let model, $rootScope, aData;

  beforeEach(inject((ResolverService, _$rootScope_, gettextCatalog) => {
    $rootScope = _$rootScope_;
    model = {
      dep: 'depValue'
    };
    aData= (model, name, data) => {
      let resolver = ResolverService.createResolver(model, name, data);
      resolver.resolve();
      resolver.watchDependencies();
    };
    gettextCatalog.setCurrentLanguage('nl');
    gettextCatalog.setStrings("nl", {
      "Hello": "Hallo"
    });
  }));

  it('should resolve expression with no dependency', () => {
    aData(model, 'myData', {
      type: 'expression',
      displayValue: 'return "myValue";'
    });

    expect(model.myData).toBe('myValue');
  });

  it('should resolve expression with dependency', () => {
    aData(model, 'myData', {
      type: 'expression',
      displayValue: 'return $data.dep + " - myValue";'
    });

    expect(model.myData).toBe('depValue - myValue');
  });

  it('should translate text', () => {
    aData(model, 'myData', {
      type: 'expression',
      displayValue: 'return uiTranslate("Hello");'
    });
    expect(model.myData).toBe('Hallo');
  });

});
