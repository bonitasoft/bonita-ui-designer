describe('Service: localStorageService', function() {

  beforeEach(module('bonitasoft.ui.services'));

  let localStorageService, $window;

  beforeEach(inject((_localStorageService_) => {
    localStorageService = _localStorageService_;
  }));

  beforeEach(inject(function ($injector, $rootScope) {
    $window = $injector.get('$window');
  }));

  beforeEach(function () {
    $window.localStorage.clear();
  });


  it('should save some data to LocalStorage', function () {
    var data = {'formInput': {'stringAttr': 'value', 'numericAttr': 7, 'objAttr': {'name': 'value'}}};
    var url = 'http://servername:8080/app/index.html?param=value&param2=value2#some-hash';

    localStorageService.save(url, data);

    expect(JSON.parse($window.localStorage.getItem('bonita-form-' + url))).toEqual(data);
  });

  it('should remove item from LocalStorage', function () {

    // Given some values stored in the LocalStorage
    var id = 'bonita-form-' + $window.location.href;
    var savedData = 'to be deleted';
    $window.localStorage.setItem( id, savedData);
    expect($window.localStorage.getItem(id)).toEqual(savedData);

    localStorageService.delete($window.location.href);

    expect($window.localStorage.getItem(id)).toEqual(null);
  });

  it('should read item from LocalStorage', function () {

    // Given some values stored in the LocalStorage
    var id = 'bonita-form-' + $window.location.href;
    var savedData = 'to be read';
    $window.localStorage.setItem( id, JSON.stringify(savedData));
    expect(JSON.parse($window.localStorage.getItem(id))).toEqual(savedData);

    expect(localStorageService.read($window.location.href)).toEqual(savedData);
  });

  describe('UIDesigner integration', function () {

    it('should ignore cache busting URL parameter when saving', function () {
      var url = 'http://servername:8080/app/index.html?param=value&param2=value2#some-hash';

      var cacheBustingQueryParameter = 'time=141211';

      var data1 = 'firstParameterIsCacheBusting';
      var url1 = 'http://servername:8080/app/index.html?' + cacheBustingQueryParameter + '&param=value&param2=value2#some-hash';
      localStorageService.save(url1, data1);
      expect(JSON.parse($window.localStorage.getItem('bonita-form-' + url1))).toEqual(null);
      expect(JSON.parse($window.localStorage.getItem('bonita-form-' + url))).toEqual(data1);

      var data2 = 'firstParameterIsCacheBusting';
      var url2 = 'http://servername:8080/app/index.html?' + cacheBustingQueryParameter + '&param=value&param2=value2#some-hash';
      localStorageService.save(url2, data2);
      expect(JSON.parse($window.localStorage.getItem('bonita-form-' + url2))).toEqual(null);
      expect(JSON.parse($window.localStorage.getItem('bonita-form-' + url))).toEqual(data2);

      var data3 = 'firstParameterIsCacheBusting';
      var url3 = 'http://servername:8080/app/index.html?' + cacheBustingQueryParameter + '&param=value&param2=value2#some-hash';
      localStorageService.save(url3, data3);
      expect(JSON.parse($window.localStorage.getItem('bonita-form-' + url3))).toEqual(null);
      expect(JSON.parse($window.localStorage.getItem('bonita-form-' + url))).toEqual(data3);

    });
  });
});
