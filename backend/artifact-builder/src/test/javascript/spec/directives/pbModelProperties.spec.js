describe('Directive: modelProperties', function () {

  beforeEach(
    module(
      'bonitasoft.ui.services',
      'bonitasoft.ui.directives',
      function ($provide) {
        $provide.value('variableModelFactory', {
          get: function (uuid) {
            return {
              'outerModel': {
                'foo': {
                  type: 'variable',
                  displayValue: 'bar'
                }
              },
              'innerModel': {
                'qux': {
                  type: 'constant',
                  displayValue: 'Hello'
                }
              }
            }[uuid];
          }
        });

        $provide.value('modelPropertiesFactory', {
          get: function (uuid) {
            return {
              'properties': {'baz': {type: 'variable', value: 'foo'}}
            }[uuid];
          }
        });
      }));

  var $scope, modelFactory, $compile, element;

  beforeEach(inject(function ($rootScope, _$compile_, _modelFactory_) {
    $compile = _$compile_;
    modelFactory = _modelFactory_;
    $scope = $rootScope.$new();

    element = $compile('<div pb-model=\'outerModel\'><div pb-model=\'innerModel\' pb-model-properties=\'properties\'></div></div>')($scope);
    $scope.$apply();
  }));

  it('should create bindings exposing parent model to the new model', function () {
    var nestedModel = element.find('div').scope().pbModelCtrl.getModel();
    expect(nestedModel.baz).toBe('bar');
    expect(nestedModel.qux).toBe('Hello');
  });

  it('should allow updating parent model through binding', function () {

    element.find('div').scope().pbModelCtrl.getModel().baz = 'foobar';

    expect(element.scope().pbModelCtrl.getModel().foo).toBe('foobar');
  });
});
