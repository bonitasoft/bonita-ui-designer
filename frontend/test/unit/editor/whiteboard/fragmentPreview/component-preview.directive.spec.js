describe('component preview', function() {
  let $compile, $rootScope, element;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;

    // given an element containing the directive
    var template = '<component-preview component="component"></component-preview>';
    // when compiling with an input
    var row = [];
    var parentContainerRow = {
      row: row
    };
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
      }
    };

    element = $compile(template)($rootScope);
    $rootScope.$digest();
  }));

  it('should display the component in preview', function() {
    // then we should have
    expect(element.html()).toBe('<div class="ng-binding ng-scope">foobar</div>');
  });

});
