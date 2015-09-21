describe('paletteService', function() {
  var paletteService;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.palette'));
  beforeEach(inject(function(_paletteService_) {
    paletteService= _paletteService_;
  }));

  it('should reset a section', function() {
    paletteService.register([{
      component:{
        id:'foo'
      },
      sectionName:'bar',
      sectionOrder: 1,
    }]);
    expect(paletteService.getSections().length).toBe(1);
    paletteService.reset();
    expect(paletteService.getSections().length).toBe(0);
  });

  it('should register a widget in a section', function() {
    expect(paletteService.getSections().length).toBe(0);
    paletteService.register([{
      component:{
        id:'foo'
      },
      sectionName:'bar',
      sectionOrder: 1,
    }]);
    var section = paletteService.getSections()[0];
    expect(section.name).toBe('bar');
    expect(section.order).toBe(1);
    expect(section.widgets.length).toBe(1);
  });

  it('should init a widget', function() {
    var spy = jasmine.createSpy('init');
    paletteService.register([{
      component:{
        id:'foo'
      },
      sectionName:'bar',
      sectionOrder: 1,
      init: spy
    }]);
    var row = {};
    var component = {id: 'foo' };
    paletteService.init(component, row);
    expect(spy).toHaveBeenCalled();
  });

  it('throw an error when trying to init an unregistered component', function() {
    function test(){
      paletteService.init({id: 'foo' });
    }
    expect(test).toThrow();
  });
});
