describe('data filters', function() {

  var data;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function($filter) {
    data = $filter('data');
  }));

  it('should return empty string for unknown data', function() {
    expect(data('foo bar')).toBe('');
  });

  it('should return empty string for data with no value', function() {
    expect(data({})).toBe('');
  });

  it('should return data value when data type is constant', function() {
    expect(data({ type: 'constant', value: 'aValue' })).toBe('aValue');
  });

  it('should return data value when data type is interpolation', function() {
    expect(data({ type: 'interpolation', value: 'aValue' })).toBe('aValue');
  });

  it('should return prefixed data value when data type is expression', function() {
    expect(data({ type: 'expression', value: 'aValue' })).toBe('data:aValue');
  });

  it('should return prefixed data value when data type is variable', function() {
    expect(data({ type: 'variable', value: 'aValue' })).toBe('data:aValue');
  });

  it('should return prefixed data value wrapped in an array for collection or choice type', function() {
    expect(data({ type: 'expression', value: 'aValue' }, 'collection')).toEqual(['data:aValue']);
  });

});
