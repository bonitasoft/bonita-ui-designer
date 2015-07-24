describe('componentScopeBuilder', function() {
  var $rootScope, componentScopeBuilder;

  beforeEach(module('bonitasoft.designer.common.services', 'bonitasoft.designer.filters'));
  beforeEach(inject(function(_componentScopeBuilder_, _$rootScope_) {
    $rootScope = _$rootScope_;
    componentScopeBuilder = _componentScopeBuilder_;
  }));

  it('should build a scope with an utility function range', function() {
    // given
    var scope = $rootScope.$new();
    scope.component = {
      $$widget: {
        properties: []
      },
      propertyValues: {
        text: { value: 'hello' }
      }
    };

    // when building a scope
    var directiveScope = componentScopeBuilder.build(scope);
    scope.$digest();

    // and we should a utility function range
    expect(directiveScope.range(5).length).toBe(5);
  });

  it('should update scope properties when global property values are updated', function() {
    var scope = $rootScope.$new();
    scope.component = {
      $$widget: {
        properties: []
      },
      propertyValues: {
        text: { value: 'hello' }
      }
    };

    var directiveScope = componentScopeBuilder.build(scope);
    scope.$digest();

    expect(directiveScope.properties.text).toBe('hello');

    scope.component.propertyValues.text.value = 'new value';
    scope.$digest();

    expect(directiveScope.properties.text).toBe('new value');
  });
});
