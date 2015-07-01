(function () {
  'use strict';

  function pendingStatus($timeout) {
    var pendingRequests = 0;
    var listenners  = [];

    this.addPendingRequest = addPendingRequest;
    this.removePendingRequest = removePendingRequest;
    this.isPending = isPending;

    this.listen = listen;

    function addPendingRequest() {
      pendingRequests += 1;
    }

    function removePendingRequest() {
      pendingRequests -= 1;

      if (pendingRequests === 0) {
        listenners.forEach(function(handler) {
           handler();
        });
      }
    }

    function isPending() {
      return pendingRequests > 0;
    }

    function listen(handler) {

      listenners = listenners.concat(handler);

        $timeout(function(){
          if(pendingRequests === 0) {
            handler();
          }
        }, 0, false);

      return function(){
        var index = listenners.indexOf(handler);
        listenners = listenners.filter(function(fn, i){
          return index != i;
        });
      };
    }


  }


  function httpActivityInterceptor($q, pendingStatus){
    return {
      request: requestHandler,
      response: responseHandler,
      responseError: responseErrorHandler
    };

    function requestHandler(config) {
      if( config.method === 'GET') {
        pendingStatus.addPendingRequest();
      }
      return config;
    }

    function responseHandler(response) {
      if (shouldRemovePendingRequest(response)) {
        pendingStatus.removePendingRequest();
      }
      return(response);
    }

    function responseErrorHandler(response){
     if (shouldRemovePendingRequest(response)) {
      pendingStatus.removePendingRequest();
     }
     return $q.reject( response ) ;
    }

    function shouldRemovePendingRequest(response){
      return  response.config && response.config.method === 'GET';
    }
  }

  angular
    .module('pb.generator.services')
    .service('pendingStatus', pendingStatus)
    .service('httpActivityInterceptor', httpActivityInterceptor);

})();
