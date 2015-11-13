(function() {

  'use strict';

  angular
    .module('mock.webSocket', [])
    .service('webSocket', webSocket);

  function webSocket($q) {

    var deferred = $q.defer();
    var listenUrl;

    return {
      listen: function(url, callback) {
        listenUrl = url;
        deferred.promise.then(callback);
      },
      send: function(url, data) {
        if (listenUrl === url) {
          deferred.resolve(data);
        }
      }
    };
  }

})();
