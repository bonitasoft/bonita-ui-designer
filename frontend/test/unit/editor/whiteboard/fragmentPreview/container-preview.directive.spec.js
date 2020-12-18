describe('container preview', function() {
  let $compile, $rootScope, element;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;

    // given an element containing the directive
    $rootScope.preview = {
      columnClasses: function() {
        return 'col-xs-12';
      }
    };
    var template = '<container-preview container="container" preview="preview" id="preview"></container-preview>';
    // when compiling with an input
    var row = [];
    var parentContainerRow = {
      row: row
    };
    $rootScope.container = {
      $$id: 'container-0',
      type: 'container',
      $$widget: {
        name: 'Container'
      },
      dimension: {
        xs: 12
      },
      rows: [
        []
      ],
      $$parentContainerRow: parentContainerRow
    };
    row.push($rootScope.container);

    element = $compile(template)($rootScope);
    $rootScope.$digest();
  }));

  it('should display the container in preview', function() {
    // then we should have the container as one row
    expect(element.find('div').length).toBe(1);
  });

  it('should display a container in a container', function() {
    $rootScope.container.rows[0].push({
      $$id: 'container-1',
      type: 'container',
      $$widget: {
        name: 'Container'
      },
      dimension: {
        xs: 12
      },
      rows: [
        []
      ],
      $$parentContainerRow: $rootScope.container.rows[0]
    });
    $rootScope.$apply();

    // then we should have the container in the container
    expect(element.find('#container-1').length).toBe(1);
    // as one row in a row
    expect(element.find('.col-xs-12 div').length).toBe(1);
  });
});
