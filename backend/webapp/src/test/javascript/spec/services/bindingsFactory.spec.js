describe('Service: bindingsFactory', function () {

  beforeEach(module('bonitasoft.ui.services'));

  var data = {
    foo: {
      type: 'variable',
      displayValue: 'bar'
    },
    baz: {
      type: 'json',
      displayValue: '{ "foo": 5 }'
    },
    qux: {
      type: 'expression',
      displayValue: 'return $data.baz.foo * 3;'
    },
    collection: {
      type: 'json',
      displayValue: '["foo", "bar", "baz", "qux"]'
    }
  };

  var properties = {
    // data are two way bound (e.g. input/output widget). Only one data can be bound.
    'foo': {
      type: 'variable',
      value: 'baz.foo'
    },

    // constants are actually one way expression. Can contains data or expression to evaluate.
    'bar': {
      type: 'interpolation',
      value: '{{ qux === 15 }}'
    },
    'baz': {
      type: 'interpolation',
      value: 'Hello {{ qux }}'
    },
    'qux': {
      type: 'constant',
      value: true
    },
    'tux': {
      type: 'interpolation',
      value: '{{ unknown }}'
    },
    'position': {
      type: 'constant',
      value: 'left'
    },
    'collection': {
      type: 'interpolation',
      value: '{{ collection }}'
    },
    'number': {
      type: 'interpolation',
      value: '{{ baz.foo }}'
    },
    'falsy': {
      type: 'interpolation',
      value: '{{ baz.foo === 7 }}'
    },
    'undefined': {
      type: 'constant',
      value: 'undefined'
    },
    'array': {
      type: 'constant',
      value: ['foo', 'bar']
    },
    'expression': {
      type: 'expression',
      value: '(1 === 1) + "" | uppercase'
    }
  };

  var componentModel, model, rootScope;

  beforeEach(inject(function (bindingsFactory, modelFactory, $rootScope) {
    model = modelFactory.create(data);
    rootScope = $rootScope;
    bindingsFactory.create(properties, model, componentModel = {});
    rootScope.$apply();
  }));

  it('should allow to retrieve a data value', function () {
    expect(componentModel.foo).toBe(5);
  });

  it('should allow update a data value', function () {
    componentModel.foo = 10;

    expect(model.baz.foo).toBe(10);
  });

  it('should interpolate string', function () {
    expect(componentModel.baz).toBe('Hello 15');
  });

  it('should return an empty string when the data is unknown from the interpolation', function () {
    expect(componentModel.tux).toBe('');
  });

  it('should allow retrieving a string value', function () {
    expect(componentModel.position).toBe('left');
  });

  it('should return an array when interpolation is an array', function () {
    expect(componentModel.array).toEqual(['foo', 'bar']);
  });

  it('should return an expression result', function () {
    expect(componentModel.expression).toEqual('TRUE');
  });

  it('should serialize properties to JSON correctly', () => {
    expect(JSON.stringify(componentModel)).toEqual('{"foo":5,"bar":"true","baz":"Hello 15","qux":true,"tux":"","position":"left","collection":"[\\"foo\\",\\"bar\\",\\"baz\\",\\"qux\\"]","number":"5","falsy":"false","undefined":"undefined","array":["foo","bar"],"expression":"TRUE"}');
  });
});
