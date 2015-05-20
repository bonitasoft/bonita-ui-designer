(function () {
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

      widgetsName[widget] += 1 ;

      return widget + widgetsName[widget];
    }
  }
  angular.module('org.bonitasoft.pagebuilder.generator.services')
    .service('widgetNameFactory', widgetNameFactory);
})();
