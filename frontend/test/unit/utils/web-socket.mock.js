(function() {

  'use strict';

  angular
    .module('mock.webSocket', [])
    .service('webSocket', webSocket);

  function webSocket($q) {

    var deferred = $q.defer();
    var deferredConnect = $q.defer();
    var listenUrls = [];

    return {
      subscribe: function(topic, callback) {
        listenUrls.push(topic);
        deferred.promise.then(callback);
      },
      send: function(url, data) {
        if (listenUrls.indexOf(url) >= 0) {
          deferred.resolve(data);
        }
      },
      connect: function() {
        return deferredConnect.promise;
      },
      resolveConnection: function () {
        deferredConnect.resolve();
      }
    };
  }

})();
