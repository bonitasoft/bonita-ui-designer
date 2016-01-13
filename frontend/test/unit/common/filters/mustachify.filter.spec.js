describe('mustachify filter', () => {

  var mustachifyFilter;

  beforeEach(angular.mock.module('bonitasoft.designer.common.filters'));

  beforeEach(inject(function(_mustachifyFilter_) {
    mustachifyFilter = _mustachifyFilter_;
  }));

  it('should replace double brackets by curly braces', function() {
    var input = mustachifyFilter('hello [[ world ]]');

    expect(input).toEqual('hello {{ world }}');
  });

  it('should replace specified characters by curly braces', function() {
    var input = mustachifyFilter('hello (( world $$', '((', '$$');

    expect(input).toEqual('hello {{ world }}');
  });
});
