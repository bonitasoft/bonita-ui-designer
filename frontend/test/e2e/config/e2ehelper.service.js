(function() {

  'use strict';

  class E2eHelper {
    getById(array, elementId) {
      return array.filter(function(elem) {
        return elem.id === elementId;
      })[0];
    }

    lastChunk(url) {
      return url.match(/([^\/]*)\/*$/)[1];
    }

    uuid() {
      return Math.floor((1 + Math.random()) * 0x10000).toString(16);
    }
  }

  angular
    .module('bonitasoft.designer.e2e')
    .service('e2ehelper', () => new E2eHelper());
})();
