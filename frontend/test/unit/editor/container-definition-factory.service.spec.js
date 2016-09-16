describe('container definition factory', function() {
  var service;

  beforeEach(angular.mock.module('bonitasoft.designer.editor'));

  beforeEach(inject(function($injector) {
    service = $injector.get('containerDefinitionFactory');
  }));

  it('should create a containerWidget', function() {
    var widget = service.createContainerWidget();
    expect(widget.id).toBe('container');
    expect(widget.name.length).toBeGreaterThan(0);
    expect(widget.order).toBeDefined();
    expect(widget.properties.length).toBe(1);
    expect(widget.icon).toMatch('<svg');
    expect(widget.custom).toBe(false);
  });

  it('should create a create a TabsContainerWidget', function() {
    var widget = service.createTabsContainerWidget();
    expect(widget.id).toBe('tabsContainer');
    expect(widget.name.length).toBeGreaterThan(0);
    expect(widget.order).toBeDefined();
    expect(widget.properties).toBeUndefined();
    expect(widget.icon).toMatch('<svg');
    expect(widget.custom).toBe(false);
  });

  it('should create a create a FormContainerWidget', function() {
    var widget = service.createFormContainerWidget();
    expect(widget.id).toBe('formContainer');
    expect(widget.name.length).toBeGreaterThan(0);
    expect(widget.order).toBeDefined();
    expect(widget.properties).toBeUndefined();
    expect(widget.icon).toMatch('<svg');
    expect(widget.custom).toBe(false);
  });
});
