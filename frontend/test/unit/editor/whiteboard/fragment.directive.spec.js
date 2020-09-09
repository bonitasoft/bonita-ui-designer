describe('fragment directive', function() {
  var $compile, $rootScope, element;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;

    // given an element containing the directive
    var template = '<fragment fragment-component="fragmentComponent" editor="editor"></fragment>';
    // when compiling with fragment component containing a widget
    var fragmentComponent = {
      $$id: 'fragment-0',
      dimension: {
        xs: 12
      },
      $$widget: {
        container: {
          $$id: 'container-0',
          type: 'container',
          dimension: {
            xs: 12,
            md: 6
          },
          rows: [
            []
          ]
        }
      }
    };
    $rootScope.fragmentComponent = fragmentComponent;

    $rootScope.editor = {
      isCurrentComponent: function() {
        return false;
      },
      isCurrentTab: function() {
        return false;
      },
      isCurrentRow: function() {
        return false;
      },
      fragmentPreview: {
        columnClasses: function() {
          return 'col-md-3';
        }
      }
    };

    element = $compile(template)($rootScope);
    $rootScope.$digest();
  }));

  it('should display the component', function() {
    // then we should have
    expect(element.find('.widget-wrapper .fragment-content container-preview').length).toBe(1);
  });
});
