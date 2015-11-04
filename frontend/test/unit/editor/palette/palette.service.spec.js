describe('paletteService', function() {
  var paletteService, components;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.palette'));
  beforeEach(inject(function(_paletteService_, _components_) {
    paletteService = _paletteService_;
    components = _components_;
  }));

  it('should get palette sections', function() {
    spyOn(components, 'get').and.returnValue({
      foo: {
        component: { id: 'foo' },
        sectionName: 'bar',
        sectionOrder: 1
      }
    });

    var section = paletteService.getSections()[0];
    expect(section.name).toBe('bar');
    expect(section.order).toBe(1);
    expect(section.widgets.length).toBe(1);
  });

});
