describe('component', function() {
  var $compile, $rootScope, element, directiveScope;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(angular.mock.module(function($provide) {
    $provide.factory('componentResizerModel', function() {
      return {
        set: angular.noop,
        isResizable: angular.noop,
        computeCols: angular.noop,
        resize: angular.noop,
        toggleVisibility: angular.noop
      };
    });
  }));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;

    // given an element containing the directive
    var template = '<component component="component" container="container" editor="editor"></component>';
    // when compiling with an input
    var row = [];
    var parentContainerRow = {
      row: row
    };
    $rootScope.container = {};
    $rootScope.component = {
      $$parentContainerRow: parentContainerRow,
      $$widget: {
        name: 'div',
        template: '<div>{{ properties.text }}</div>',
        properties: [
          {
            name: 'text',
            type: 'string'
          }
        ]
      },
      propertyValues: {
        text: { value: 'foobar' }
      }
    };
    row.push($rootScope.component);

    $rootScope.editor = {
      isCurrentComponent: function() {
        return false;
      },
      page: { id: 'c0eae20f-14dd-4312-a678-2f1fab0a3898' }
    };

    element = $compile(template)($rootScope);
    directiveScope = element.isolateScope();
    $rootScope.$digest();
  }));

  it('should display the component in page', function() {
    // then we should have
    expect(element.find('.widget-wrapper').length).toBe(1);
    expect(element.find('.widget-wrapper .widget-content').length).toBe(1);
  });

  it('should display the component text property value when it is set', function() {
    // then we should have
    expect(element.html()).toContain('foobar');
  });
});
