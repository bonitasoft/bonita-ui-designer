(function() {
  'use strict';

  function widgetNameFactory() {
    var widgetsName = {};

    return {
      getName: getName,
      getId: getId
    };

    function getName(widget) {
      if (!widgetsName.hasOwnProperty(widget)) {
        widgetsName[widget] = -1;
      }

      widgetsName[widget] += 1;

      return widget + widgetsName[widget];
    }

    function getId(widget){
      return `${widget + widgetsName[widget]}-input`;
    }

  }
  angular.module('bonitasoft.ui.services')
    .service('widgetNameFactory', widgetNameFactory);
})();
