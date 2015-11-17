(function() {
  'use strict';

  function widgetNameFactory() {
    var widgetsName = {};

    return {
      getName: getName
    };

    function getName(widget) {
      if (!widgetsName.hasOwnProperty(widget)) {
        widgetsName[widget] = -1;
      }

      widgetsName[widget] += 1;

      return widget + widgetsName[widget];
    }
  }
  angular.module('bonitasoft.ui.services')
    .service('widgetNameFactory', widgetNameFactory);
})();
