angular.module('bonitasoft.ui.i18n').factory('i18n', function(gettextCatalog, $cookies, $window, localizationFactory, i18nCodeMapping, $locale) {

  return {
    init() {
      let language = $cookies.BOS_Locale ||
        $window.navigator.language ||
        $window.navigator.userLanguage || // IE
        'en-US';

      let localization = localizationFactory.get();
      let languages = Object.keys(localization);
      let currentCode = i18nCodeMapping.get(language, languages);
      if (localization && localization[currentCode] && localization[currentCode].$locale) {
        angular.merge($locale, localization[currentCode].$locale);
      }

      languages.forEach(function(language) {
        gettextCatalog.setStrings(language, localization[language]);
      });

      gettextCatalog.setCurrentLanguage(currentCode);

      return currentCode;
    }
  };
});
