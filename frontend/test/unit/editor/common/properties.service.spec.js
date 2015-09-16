describe('properties service', function () {

  var service;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.common'));

  beforeEach(inject(function (properties) {
    service = properties;
  }));

  describe('should compute a property value from widget property', function () {

    it('and populate propertyValue value with property default value', function () {
      expect(service.computeValue({defaultValue: 'default'}).value).toEqual('default');
    });

    it('and set propertyValue type to constant when property bond is expression', function () {
      expect(service.computeValue({bond: 'expression'}).type).toEqual('constant');
    });

    it('and set propertyValue type to property bond otherwise', function () {
      expect(service.computeValue({bond: 'constant'}).type).toEqual('constant');
      expect(service.computeValue({bond: 'interpolation'}).type).toEqual('interpolation');
      expect(service.computeValue({bond: 'variable'}).type).toEqual('variable');
    });
  });

  it('should compute all property values of a component', function () {
    var properties = [
      {name: 'anExpression', defaultValue: 'default expression', bond: 'expression'},
      {name: 'aConstant', defaultValue: 'default constant', bond: 'constant'},
      {name: 'anInterpolation', defaultValue: 'default interpolation', bond: 'interpolation'},
      {name: 'aVariable', defaultValue: 'default variable', bond: 'variable'}
    ];

    var propertyValues = service.computeValues(properties);

    expect(propertyValues).toEqual({
      anExpression: {value: 'default expression', type: 'constant'},
      aConstant: {value: 'default constant', type: 'constant'},
      anInterpolation: {value: 'default interpolation', type: 'interpolation'},
      aVariable: {value: 'default variable', type: 'variable'}
    });

    it('should concat common properties to a component properties', function () {
      var properties = [
        {name: 'anExpression', defaultValue: 'default expression', bond: 'expression'},
        {name: 'aConstant', defaultValue: 'default constant', bond: 'constant'}
      ];
      var aComponent = {properties};

      service.addCommonPropertiesTo(aComponent);

      expect(aComponent.properties).toContain(properties);
      expect(aComponent.properties).toContain([
        {
          label: 'CSS classes',
          caption: 'Space-separated list',
          name: 'cssClasses',
          type: 'string',
          defaultValue: '',
          bond: 'expression',
          help: 'Any accessible CSS classes. By default UI Designer comes with Bootstrap http://getbootstrap.com/css/#helper-classes'
        },
        {
          label: 'Hidden',
          name: 'hidden',
          type: 'boolean',
          defaultValue: false,
          bond: 'expression'
        }]);
    });
  });
});
