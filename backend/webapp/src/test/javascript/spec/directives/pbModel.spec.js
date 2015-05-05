describe('Directive: model', function () {

  beforeEach(
    module(
      'org.bonitasoft.pagebuilder.generator.services',
      'org.bonitasoft.pagebuilder.generator.directives',
      function ($provide) {
        $provide.value('dataModelFactory', {
          get: function (uuid) {
            return {
              'outerModel': data,
              'innerModel': {
                baz: {
                  type: 'variable',
                  value: 'qux'
                }
              }
            }[uuid];
          }
        });
      }));

  var $scope, modelFactory, $compile, data = {
    'foo': {
      type: 'variable',
      value: 'bar'
      }
    };

  beforeEach(inject(function($rootScope, _$compile_, _modelFactory_) {
    $compile = _$compile_;
    modelFactory = _modelFactory_;
    $scope = $rootScope.$new();
  }));

  it('should expose a model from the data via a model controller', function() {
    var element = $compile('<div pb-model=\'outerModel\'></div>')($scope);
    $scope.$apply();

    var model = element.scope().pbModelCtrl.getModel();

    expect(JSON.stringify(model)).toBe(JSON.stringify(modelFactory.create(data)));
  });

  it('should expose a method to retrieve a value from the model', function() {
    var element = $compile('<div pb-model=\'outerModel\'></div>')($scope);
    $scope.$apply();

    expect(element.scope().pbModelCtrl.getModel().foo).toBe('bar');
  });

  it('should mask previous scope model', function() {
    var element = $compile('<div pb-model=\'outerModel\'>' +
    '<div pb-model=\'innerModel\'></div></div>')($scope);
    $scope.$apply();

    expect(element.scope().pbModelCtrl.getModel().foo).toBe('bar');
    var nestedModel = element.find('div').scope().pbModelCtrl.getModel();
    expect(nestedModel.foo).toBeUndefined();
    expect(nestedModel.baz).toBe('qux');
  });
});
