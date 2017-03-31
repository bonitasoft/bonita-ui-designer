describe('uiDate filter', function () {

  beforeEach(module('bonitasoft.ui.filters'));

  var uiDateFilter;

  beforeEach(inject(function (_uiDateFilter_) {

    uiDateFilter = _uiDateFilter_;

  }));

  it('should convert long to date by default', function () {
    expect(uiDateFilter(1490918400000)).toBe('Mar 31, 2017');
  });

  it('should convert long to UTC date', function () {
    expect(uiDateFilter(1490918400000, 'medium')).toBe('Mar 31, 2017 12:00:00 AM');
  });

  it('should convert ISO string to date', function () {
    expect(uiDateFilter('2017-03-31')).toBe('Mar 31, 2017');
  });

  it('should convert ISO string to date and time', function () {
    expect(uiDateFilter('2017-03-31T00:00:00')).toBe('Mar 31, 2017 12:00:00 AM');
  });

  it('should convert ISO string with timezone to date and time', function () {
    expect(uiDateFilter('2017-03-31T00:00:00Z')).toContain('Mar 31, 2017');
    expect(uiDateFilter('2017-03-31T00:00:00Z')).not.toBe('Mar 31, 2017');
  });

});
