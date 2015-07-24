describe('data filters', function() {

  var data;

  beforeEach(module('bonitasoft.designer.filters'));

  beforeEach(inject(function($filter) {
    data = $filter('data');
  }));

  it('should return empty string for unknown data', function() {
    expect(data('foo bar')).toBe('');
  });

  it('should return empty string for data with no value', function() {
    expect(data({})).toBe('');
  });

  it('should return data value when data type is constant', function () {
    expect(data({type: 'constant', value: 'aValue'})).toBe('aValue');
  });

  it('should return prefixed data value when data type is data', function () {
    expect(data({type: 'data', value: 'aValue'})).toBe('data:aValue');
  });

  it('should return prefixed data value wrap in an array for collection or choice type', function () {
    expect(data({type: 'data', value: 'aValue'}, 'collection')).toEqual(['data:aValue']);
  });

});
