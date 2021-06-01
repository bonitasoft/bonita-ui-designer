(function() {

  'use strict';

  angular
    .module('bonitasoft.ui.i18n')
    .factory('i18nCodeMapping', i18nCodeMapping);

  function i18nCodeMapping() {

    return {
      get
    };

    /**
     * Matches first the exact code, then matches
     * - fr  <=>  fr-FR || fr-fr || fr_FR || fr_fr
     * - fr-FR <=> fr_FR || fr_fr
     * - fr_FR <=> fr-FR || fr-fr
     * - fr-BE <=> fr
     * - fr_BE <=> fr
     * - ja <=> ja-JP
     * - ja-JP <=> ja-JP
     */
    function get(code, codes) {
      // Particular use case for Japanese
      code = code === 'ja' ? 'ja-JP' : code;
      return match(codes, code) ||
        match(codes, code + '_' + code) ||
        match(codes, code.replace('-', '_')) ||
        match(codes, code.substring(0, code.indexOf('-'))) ||
        match(codes, code.substring(0, code.indexOf('_'))) ||
        code;
    }

    function match(codes, pattern) {
      return (codes.filter((c) => c.toLowerCase().replace('_', '-') === pattern.toLowerCase().replace('_', '-')))[0];
    }
  }

})();
