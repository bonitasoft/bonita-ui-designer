describe('components service', function() {
  var components;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.common'));
  beforeEach(inject(function(_components_) {
    components = _components_;
  }));

  it('should reset registered components', function() {
    components.register([{
      component: {
        id: 'foo'
      },
      sectionName: 'bar',
      sectionOrder: 1
    }]);
    expect(Object.keys(components.get()).length).toBe(1);

    components.reset();

    expect(Object.keys(components.get()).length).toBe(0);
  });

  it('should register components', function() {

    components.register([{
      component: {
        id: 'foo'
      },
      sectionName: 'bar',
      sectionOrder: 1
    }]);

    expect(components.get()).toEqual({
      foo: {
        component: { id: 'foo' },
        sectionName: 'bar',
        sectionOrder: 1
      }
    });
  });

  it('should init a widget', function() {
    var spy = jasmine.createSpy('init');
    components.register([{
      component: {
        id: 'foo'
      },
      sectionName: 'bar',
      sectionOrder: 1,
      init: spy
    }]);
    var row = {};
    var component = { id: 'foo' };

    components.init(component, row);

    expect(spy).toHaveBeenCalled();
  });

  it('throw an error when trying to init an unregistered component', function() {
    function test() {
      components.init({ id: 'foo' });
    }
    expect(test).toThrow();
  });
});
