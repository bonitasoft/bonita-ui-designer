describe('i18n', function () {

  var i18n, gettextCatalog, $cookies, $window, $q, localization, $locale;

  beforeEach(module('bonitasoft.ui.i18n', function ($provide) {
    localization = {
      'fr-FR': {
        'Hello': 'Bonjour',
        '$locale': {
          'DATETIME_FORMATS': {
            'DAY': [
                 'dimanche',
                 'lundi',
                 'mardi',
                 'mercredi',
                 'jeudi',
                 'vendredi',
                 'samedi'
              ]
            }
          }
        }
      };
    $provide.value('localizationFactory', {
      get: function () {
        return localization;
      }
    });
  }));
  beforeEach(inject(function ($injector, _$locale_) {
    i18n = $injector.get('i18n');
    gettextCatalog = $injector.get('gettextCatalog');
    $cookies = $injector.get('$cookies');
    $window = $injector.get('$window');
    $q = $injector.get('$q');
    $locale = _$locale_;
  }));

  it('should pick up BOS_Locale cookie value', function () {
    $cookies.BOS_Locale = 'fr-FR';

    i18n.init();

    expect(gettextCatalog.getCurrentLanguage()).toBe('fr-FR');
    expect($locale.DATETIME_FORMATS.DAY[0]).toEqual('dimanche');
  });

  it('should pick up navigator language value', function () {
    $window.navigator = {
      language: 'fr-FR'
    };

    i18n.init();

    expect(gettextCatalog.getCurrentLanguage()).toBe('fr-FR');
  });

  it('should pick up ie language value', function () {
    $window.navigator = {
      userLanguage: 'fr-FR'
    };

    i18n.init();

    expect(gettextCatalog.getCurrentLanguage()).toBe('fr-FR');
  });

  it('should default to en when no preference is defined', function () {
    $window.navigator = {};

    i18n.init();

    expect(gettextCatalog.getCurrentLanguage()).toBe('en-US');
  });

  it('should normalize locale when coming from portal', function () {
    $cookies.BOS_Locale = 'it';
    localization = {'it-IT': {'Hello': 'Bonjourno'}};

    i18n.init();

    expect(gettextCatalog.getCurrentLanguage()).toBe('it-IT');
    expect($locale.DATETIME_FORMATS.DAY[0]).toEqual('Sunday');
    expect($locale.NUMBER_FORMATS).not.toBeUndefined();
  });

  it('should set localization strings', function () {
    spyOn(gettextCatalog, 'setStrings');

    i18n.init();

    expect(gettextCatalog.setStrings).toHaveBeenCalledWith('fr-FR', localization['fr-FR']);
  });

  it('should merge $locale date time format with those of the localization ones', () => {
    $cookies.BOS_Locale = 'fr';
    localization = {
      'fr-FR': {
        'Hello': 'Bonjour', 
        '$locale': {
          'DATETIME_FORMATS': {
            'DAY': [
              'dimanche',
              'lundi',
              'mardi',
              'mercredi',
              'jeudi',
              'vendredi',
              'samedi'
            ]
          }
        }
      }
    };
    i18n.init();
    expect($locale.DATETIME_FORMATS.longDate).toEqual('MMMM d, y');
    expect($locale.DATETIME_FORMATS.DAY[0]).toEqual('dimanche');
  });

  it('should set localization strings', function () {
    spyOn(gettextCatalog, 'setStrings');

    i18n.init();

    expect(gettextCatalog.setStrings).toHaveBeenCalledWith('fr-FR', localization['fr-FR']);
  });
});
