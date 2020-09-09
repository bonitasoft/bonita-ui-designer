describe('fragment service', function() {

  var service, components, dimensions, item, fragment, parentRow,whiteboardComponentWrapper;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function($injector, resolutions) {
    service = $injector.get('fragmentService');
    components = $injector.get('components');
    whiteboardComponentWrapper = $injector.get('whiteboardComponentWrapper');
    spyOn(whiteboardComponentWrapper, 'wrapContainer');
    dimensions = {
      xs: 12,
      md: 12,
      sm: 12,
      lg: 12
    };
    resolutions.setDefaultDimension(dimensions);
  }));

  beforeEach(function() {
    parentRow = {};
    item = {
      rows: []
    };

    fragment = {
      id: 'f1',
      type: 'fragment',
      name: 'foo',
      rows: [
        [
          {
            type: 'component',
            id: 'label'
          }
        ]
      ],
      properties: [{
        name: 'aProp',
        defaultValue: 'aValue',
        bond: 'expression'
      }]
    };

  });

  it('should create a register fragment in palette', function() {
    spyOn(components, 'register');

    service.register([fragment]);
    expect(components.register).toHaveBeenCalled();
  });

  it('should create a palette item', function() {
    var item = service.createPaletteItem(fragment);
    expect(item.create).toBeDefined();
    expect(item.init).toBeDefined();
    expect(item.component).toBe(fragment);
    expect(item.sectionName).toBe('fragments');
    expect(item.sectionOrder).toBe(3);
  });

  it('should createFragment a fragment component', function() {
    spyOn(whiteboardComponentWrapper, 'wrapFragment');

    item = service.createFragment(fragment, parentRow);
    expect(item.type).toBe('fragment');
    expect(item.id).toBe('f1');
    expect(item.dimension).toEqual(dimensions);
    expect(Object.keys(item.propertyValues).length).toEqual(fragment.properties.length);

    expect(whiteboardComponentWrapper.wrapFragment).toHaveBeenCalled();
  });

});
