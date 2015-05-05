describe('Directive: modelProperties', function () {

  beforeEach(
    module(
      'org.bonitasoft.pagebuilder.generator.services',
      'org.bonitasoft.pagebuilder.generator.directives',
      function ($provide) {
        $provide.value('dataModelFactory', {
          get: function (uuid) {
            return {
              'outerModel': {
                'foo': {
                  type: 'variable',
                  value: 'bar'
                }
              },
              'innerModel': {
                'qux': {
                  type: 'constant',
                  value: 'Hello'
                }
              }
            }[uuid];
          }
        });
      }));

  var properties = {
    'baz': {type: 'data', value: 'foo'}
  };

  var $scope, modelFactory, $compile, element;

  beforeEach(inject(function ($rootScope, _$compile_, _modelFactory_) {
    $compile = _$compile_;
    modelFactory = _modelFactory_;
    $scope = $rootScope.$new();

    $scope.properties = properties;
    element = $compile('<div pb-model=\'outerModel\'><div pb-model=\'innerModel\' pb-model-properties=\'' + JSON.stringify(properties) + '\'></div></div>')($scope);
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
