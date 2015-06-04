describe('Directive: propertyValues', function () {

  beforeEach(
    module(
      'pb.generator.services',
      'pb.generator.directives',
      function ($provide) {
        $provide.value('propertyValuesFactory', {
          get: function (uuid) {
            return {
              'properties': properties,
              'containerProperties': containerProperties
            }[uuid];
          }
        });
        $provide.value('dataModelFactory', {
          get: function (uuid) {
            return {
              'dataModel': data
            }[uuid];
          }
        });
        $provide.value('modelPropertiesFactory', {
          get: function () {
          }
        });
      }
    ));

  var data = {
    'foo': {
      type: 'variable',
      value: 'bar'
    },
    'collection': {
      type: 'json',
      value: '["Vincent", "Amandine"]'
    }
  };

  var properties = {
    'baz': {
      type: 'constant',
      value: '{{ foo }}'
    },
    'twoWayBaz': {
      type: 'data',
      value: 'foo'
    }
  };

  var containerProperties = {
    'repeatedCollection': {
      type: 'data',
      value: 'collection'
    }
  };

  var $scope, modelFactory, $compile;

  beforeEach(inject(function ($rootScope, _$compile_, _modelFactory_) {
    $compile = _$compile_;
    modelFactory = _modelFactory_;
    $scope = $rootScope.$new();
    $scope.properties = properties;
  }));

  function compileTemplate() {
    return $compile(
      '<div pb-model=\'dataModel\'><div pb-property-values=\'properties\'></div></div>')($scope);
  }

  it('should allow accessing model via bindings', function () {
    var element = compileTemplate();
    $scope.$apply();

    expect(element.find('div').scope().properties.baz).toBe('bar');
  });

  it('should allow updating model via bindings on data', function () {
    var element = compileTemplate();
    $scope.$apply();

    element.find('div').scope().properties.twoWayBaz = 'foobar';

    expect(element.scope().pbModelCtrl.getModel().foo).toBe('foobar');
  });

  it('should allow updating model via bindings even after emptying it', function () {
    var element = compileTemplate();
    $scope.$apply();

    element.find('div').scope().properties.twoWayBaz = '';
    element.find('div').scope().properties.twoWayBaz = 'baz';

    expect(element.scope().pbModelCtrl.getModel().foo).toBe('baz');
  });

  it('should return true when data is bound', function () {
    var element = compileTemplate();
    $scope.$apply();

    expect(element.find('div').scope().properties.isBound('twoWayBaz')).toBe(true);
  });

  it('should return false when data is not bound', function () {
    var element = compileTemplate();
    $scope.$apply();

    expect(element.find('div').scope().properties.isBound('baz')).toBe(false);
  });

  it('should return false when data does not exist', function () {
    var element = compileTemplate();
    $scope.$apply();

    expect(element.find('div').scope().properties.isBound('notExisting')).toBe(false);
  });

  describe('in a repeated context', function () {

    function compileRepeatedTemplate() {
      return $compile('<div pb-model=\'dataModel\'>' +
        '<div class="container" pb-property-values=\'containerProperties\'>' +
        '<div ng-repeat="$item in properties.repeatedCollection" ng-init="$collection = properties.repeatedCollection">' +
        '<div class="widget" pb-property-values=\'properties\'></div></div></div></div>')($scope);
    }

    it('should allow access to the current value of the iteration', function () {
      properties.qux = {
        type: 'constant',
        value: 'Hello {{ $item }}'
      };

      var element = compileRepeatedTemplate();
      $scope.$apply();

      expect([].map.call(element.find('.widget'), function (widget) {
        return angular.element(widget).scope().properties.qux;
      })).toEqual(['Hello Vincent', 'Hello Amandine']);
    });

    it('should allow two way binding on data', function () {
      properties.qux = {
        type: 'data',
        value: '$item'
      };

      var element = compileRepeatedTemplate();
      $scope.$apply();

      angular.element(element.find('.widget')[0]).scope().properties.qux = 'Thomas';
      expect(element.scope().pbModelCtrl.getModel().collection[0]).toBe('Thomas');
    });
  });
});
