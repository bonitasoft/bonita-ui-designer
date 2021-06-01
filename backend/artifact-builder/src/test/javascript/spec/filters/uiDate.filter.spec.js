describe('uiDate filter', function () {

  beforeEach(module('bonitasoft.ui.filters'));

  var uiDateFilter, $filter;

  beforeEach(inject(function (_uiDateFilter_, _$filter_) {

    uiDateFilter = _uiDateFilter_;
    $filter = _$filter_;

  }));

  it('should convert long to date by default', function () {
    expect(uiDateFilter(1490918400000)).toBe('Mar 31, 2017');
  });

  it('should convert long to UTC date', function () {
    expect(uiDateFilter(1490918400000, 'yyyy/MM/dd HH:mm:ss')).toBe('2017/03/31 00:00:00');
  });

  it('should convert ISO string to date', function () {
    expect(uiDateFilter('2017-03-31')).toBe('Mar 31, 2017');
  });

  it('should convert ISO string to date with format', function () {
    expect(uiDateFilter('2017-03-31', 'yyyy/MM/dd')).toBe('2017/03/31');
  });

  it('should convert ISO string to date and time', function () {
    expect(uiDateFilter('2017-03-31T00:00:00')).toBe('Mar 31, 2017 12:00:00 AM');
  });

  it('should convert ISO string to date and time with format', function () {
    expect(uiDateFilter('2017-03-31T00:00:00', 'yyyy/MM/dd HH:mm:ss')).toBe('2017/03/31 00:00:00');
  });

  it('should convert ISO string with timezone to date and time', function () {
    var isoDateWithLocalTimezone = uiDateFilter('2017-03-31T00:00:00Z', 'yyyy-MM-ddTHH:mm:ssZ');
    expect($filter('date')(isoDateWithLocalTimezone, 'yyyy-MM-ddTHH:mm:ss', 'UTC')).toBe('2017-03-31T00:00:00');
  });

});
