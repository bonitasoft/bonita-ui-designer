(function() {

  'use strict';

  angular
      .module('bonitasoft.designer.editor.whiteboard')
      .service('componentId', componentIdService);

  function componentIdService() {

    var counters = {};
    return {
      getNextId: getNextId
    };

    function getNextId(type) {
      if (counters.hasOwnProperty(type)) {
        counters[type] += 1;
      } else {
        counters[type] = 0;
      }
      return type + '-' + counters[type];
    }
  }

})();
