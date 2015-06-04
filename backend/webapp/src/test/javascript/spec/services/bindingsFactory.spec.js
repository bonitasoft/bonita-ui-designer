describe('Service: bindingsFactory', function () {

  beforeEach(module('pb.generator.services'));

  var data = {
    foo: {
      type: 'variable',
      value: 'bar'
    },
    baz: {
      type: 'json',
      value: '{ "foo": 5 }'
    },
    qux: {
      type: 'expression',
      value: 'return $data.baz.foo * 3;'
    },
    collection: {
      type: 'json',
      value: '["foo", "bar", "baz", "qux"]'
    }
  };

  var properties = {
    // data are two way bound (e.g. input/output widget). Only one data can be bound.
    'foo': {
      type: 'data',
      value: 'baz.foo'
    },

    // constants are actually one way expression. Can contains data or expression to evaluate.
    'bar': {
      type: 'constant',
      value: '{{ qux === 15 }}'
    },
    'baz': {
      type: 'constant',
      value: 'Hello {{ qux }}'
    },
    'qux': {
      type: 'constant',
      value: true
    },
    'tux': {
      type: 'constant',
      value: '{{ unknown }}'
    },
    'position': {
      type: 'constant',
      value: 'left'
    },
    'collection': {
      type: 'constant',
      value: '{{ collection }}'
    },
    'number': {
      type: 'constant',
      value: '{{ baz.foo }}'
    },
    'falsy': {
      type: 'constant',
      value: '{{ baz.foo === 7 }}'
    },
    'undefined': {
      type: 'constant',
      value: 'undefined'
    },
    'array': {
      type: 'constant',
      value: ['foo', 'bar']
    }
  };

  var componentModel, model;

  beforeEach(inject(function (bindingsFactory, modelFactory) {
    model = modelFactory.create(data);
    bindingsFactory.create(properties, model, componentModel = {});
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
});
