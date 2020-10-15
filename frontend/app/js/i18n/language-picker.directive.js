/*******************************************************************************
 * Copyright (C) 2009, 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.i18n')
    .directive('languagePicker', languagePicker);

  function languagePicker() {
    return {
      scope: {
        onChange: '&'
      },
      restrict: 'E',
      templateUrl: 'js/i18n/language-picker.html',
      controller: LanguagePickerCtrl,
      controllerAs: 'vm',
      bindToController: true
    };
  }

  function LanguagePickerCtrl($scope, i18n) {
    var vm = this;
    vm.lang = i18n.defaultLanguage();
    vm.langs = i18n.defaultLanguages();

    vm.chooseLang = chooseLang;

    i18n.onRefresh(onI18nRefresh);
    $scope.$on('$destroy', () => i18n.cleanLanguage());

    function chooseLang(lang) {
      vm.lang = lang;
      i18n.selectLanguage(lang);
      vm.onChange();
    }

    function onI18nRefresh(langs, lang) {
      $scope.$apply(function() {
        vm.lang = lang;
        vm.langs = langs;
      });
    }
  }

})();
