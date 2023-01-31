angular.module('bonitasoft.ui').run(function(i18n) {
  let locale = i18n.init();
  document.documentElement.setAttribute('lang', locale);
});
