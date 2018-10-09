angular.module('bonitasoft.ui.services').factory('localizationFactory', function() {
  return {
    get: function() {
      return {{{ localization }}};
    }
  };
});
