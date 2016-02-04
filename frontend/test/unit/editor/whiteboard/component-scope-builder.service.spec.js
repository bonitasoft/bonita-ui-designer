describe('componentScopeBuilder', function() {
  var $rootScope, componentScopeBuilder;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function(_componentScopeBuilder_, _$rootScope_) {
    $rootScope = _$rootScope_;
    componentScopeBuilder = _componentScopeBuilder_;
  }));

  it('should build a scope with an utility function range', function() {
    // given
    var scope = $rootScope.$new();
    scope.component = {
      id: 'dumbWidget',
      $$widget: {
        id: 'dumbWidget',
        name: 'dumbWidget',
        properties: []
      },
      propertyValues: {
        text: { value: 'hello' }
      }
    };
    scope.editor = { page: { id: 'c0eae20f-14dd-4312-a678-2f1fab0a3898' } };

    // when building a scope
    var directiveScope = componentScopeBuilder.build(scope);
    scope.$digest();

    // and we should a utility function range
    expect(directiveScope.range(5).length).toBe(5);
    expect(directiveScope.environment.editor.pageId).toEqual('c0eae20f-14dd-4312-a678-2f1fab0a3898');
    expect(directiveScope.environment.component).toBe(scope.component.$$widget);
  });

  it('should update scope properties when global property values are updated', function() {
    var scope = $rootScope.$new();
    scope.component = {
      id: 'dumbWidget',
      $$widget: {
        id: 'dumbWidget',
        name: 'dumbWidget',
        properties: []
      },
      propertyValues: {
        text: { value: 'hello' }
      }
    };
    scope.editor = { page: { id: 'c0eae20f-14dd-4312-a678-2f1fab0a3898' } };

    var directiveScope = componentScopeBuilder.build(scope);
    scope.$digest();

    expect(directiveScope.properties.text).toBe('hello');

    scope.component.propertyValues.text.value = 'new value';
    scope.$digest();

    expect(directiveScope.properties.text).toBe('new value');
  });

  it('should not break when editor does not exists', function() {
    var scope = $rootScope.$new();
    scope.component = {
      $$widget: {
        id: 'dumbWidget',
        name: 'dumbWidget',
        properties: []
      },
      propertyValues: {
        text: { value: 'hello' }
      }
    };

    componentScopeBuilder.build(scope);

  });
});
