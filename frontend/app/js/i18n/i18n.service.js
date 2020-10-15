(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.i18n')
    .service('i18n', i18nService);

  function i18nService($cookies) {
    const defaultLang = 'Default';
    var callback = angular.noop;
    var cookiePath = '/';

    return {
      refresh,
      onRefresh,
      defaultLanguages,
      defaultLanguage,
      setCookiePath,
      selectLanguage,
      cleanLanguage
    };

    function onRefresh(cb) {
      callback = cb;
    }

    function refresh(availableLangs, selectedLang) {
      var languages = defaultLanguages().concat(availableLangs);
      var language = languages.reduce(function(acc, lang) {
        if (selectedLang === lang) {
          acc = lang;
        }
        return acc;
      }, defaultLang);
      callback(languages, language);
    }

    function defaultLanguages() {
      return [defaultLang];
    }

    function defaultLanguage() {
      return defaultLang;
    }

    function setCookiePath(path) {
      cookiePath = path;
    }

    function selectLanguage(lang) {
      $cookies.put('BOS_Locale', lang, { path: cookiePath });
    }

    function cleanLanguage() {
      $cookies.remove('BOS_Locale', { path: cookiePath });
    }
  }
})();
