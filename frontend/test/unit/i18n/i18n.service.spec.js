describe('i18n service', function() {

  var i18n, callback, $cookies;

  beforeEach(angular.mock.module('bonitasoft.designer.i18n'));

  beforeEach(inject(function(_i18n_, _$cookies_) {
    i18n = _i18n_;
    $cookies = _$cookies_;
    callback = jasmine.createSpy('callback');
  }));

  it('should call registered callback while refreshing i18n', function() {

    i18n.onRefresh(callback);

    i18n.refresh();
    expect(callback).toHaveBeenCalled();
  });

  it('should concat default language to available ones', function() {
    i18n.onRefresh(callback);

    i18n.refresh(['fr-FR', 'es-ES'], 'fr-FR');

    expect(callback).toHaveBeenCalledWith(['Default', 'fr-FR', 'es-ES'], jasmine.any(String));
  });

  it('should set selected language if it is contained in available languages', function() {
    i18n.onRefresh(callback);

    i18n.refresh(['fr-FR', 'es-ES'], 'fr-FR');

    expect(callback).toHaveBeenCalledWith(['Default', 'fr-FR', 'es-ES'], 'fr-FR');
  });

  it('should set default language if it is not contained in available languages', function() {
    i18n.onRefresh(callback);

    i18n.refresh(['fr-FR', 'es-ES'], 'notexisting');

    expect(callback).toHaveBeenCalledWith(['Default', 'fr-FR', 'es-ES'], 'Default');
  });

  it('should get default languages', function() {
    expect(i18n.defaultLanguages()).toEqual(['Default']);
  });

  it('should get default selected language', function() {
    expect(i18n.defaultLanguage()).toEqual('Default');
  });

  it('should save selected language in a specific cookie with a specific path', function() {
    spyOn($cookies, 'put');
    i18n.setCookiePath('/a/specific/path');

    i18n.selectLanguage('aLang');

    expect($cookies.put).toHaveBeenCalledWith('BOS_Locale', 'aLang', {path: '/a/specific/path'});
  });

  it('should clean cookie for the specific path', function() {
    spyOn($cookies, 'remove');
    i18n.setCookiePath('/a/specific/path');

    i18n.cleanLanguage();

    expect($cookies.remove).toHaveBeenCalledWith('BOS_Locale', {path: '/a/specific/path'});
  });
});
