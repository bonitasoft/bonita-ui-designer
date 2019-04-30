describe('lazyRef filter', function () {

  beforeEach(module('bonitasoft.ui.filters'));

  var lazyRefFilter;
  var $log;
  var aBo;
  var aBoWithoutLinks;

  beforeEach(inject(function (_lazyRefFilter_,_$log_) {

    lazyRefFilter = _lazyRefFilter_;
    $log = _$log_;
    spyOn($log, 'warn');
    aBo = {
      'name' : 'Romain',
      'links' : [
        {
          'rel' : 'address',
          'href' : '/an/url/to/data'
        }
      ]
    };
    aBoWithoutLinks = {
      'name' : 'Romain'
    }

  }));

  it('should retrieve url of a lazy relation field', function () {
    expect(lazyRefFilter(aBo,'address')).toBe('../an/url/to/data');
  });

  it('should log a warning when no specific lazy relation is found', function () {
    expect(lazyRefFilter(aBo,'perks')).toBe(undefined);
    expect( $log.warn).toHaveBeenCalledWith('No lazy relation ', 'perks',' found');
  });

  it('should not log a warning when no lazy relation are available', function () {
    expect(lazyRefFilter(aBoWithoutLinks,'perks')).toBe(undefined);
    expect($log.warn).not.toHaveBeenCalled();
  });

  it('should not log a warning when the business object is not loaded', function () {
    expect(lazyRefFilter(undefined,'address')).toBe(undefined);
    expect($log.warn).not.toHaveBeenCalled();
  });

});
