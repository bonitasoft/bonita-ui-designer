(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.i18n')
    .directive('previewLocalizedIframe', previewIframe)
    .service('iframeElement', iframeElement);

  function previewIframe(i18n, iframeElement) {
    return {
      link
    };

    function link(scope, element) {
      var iframe = iframeElement.get(element[0]);

      element.on('load', function() {
        //check https://github.com/angular/angular.js/issues/7023
        var catalog = iframe.angular.element(iframe.document.body).injector().get('gettextCatalog');
        i18n.refresh(Object.keys(catalog.strings), catalog.getCurrentLanguage());
      });
      scope.$on('$destroy', () => element.off('load'));

      i18n.setCookiePath(iframe.location.src);
    }
  }

  /**
   * Service used for test purpose.
   * Allow to mock iframe
   */
  function iframeElement() {
    return {
      get: function(iframe) {
        return iframe.contentWindow;
      }
    };
  }

})();
