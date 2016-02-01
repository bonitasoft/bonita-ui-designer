describe('paletteWidget', function() {
  var $compile, $rootScope, element, directiveScope;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.palette'));
  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;

    // given an element containing the directive
    var template = '<palette-widget widget="widget" ></palette-widget>';
    // when compiling with an input
    $rootScope.widget = {
      component: {
        id: 'w-input',
        name: 'input'
      }
    };
    element = $compile(template)($rootScope);
    $rootScope.$digest();
    directiveScope = element.isolateScope();
  }));

  it('should display the widget in palette', function() {
    // then we should have
    expect(element.find('div[bo-draggable]').attr('id')).toBe('w-input');
    expect(element.text()).toContain('input');
  });

});
