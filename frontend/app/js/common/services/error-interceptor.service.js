/**
 * http response interceptor which extracts the error message from an error http response and adds it to the alerts
 * service
 */
angular.module('pb.common.services').factory('errorInterceptor', function($q, alerts) {

  'use strict';

  return {
    responseError: function(rejection) {
      if (rejection.headers('Content-Type') &&
          rejection.headers('Content-Type').indexOf('application/json') === 0 &&
          angular.isDefined(rejection.data.message)) {
        alerts.addError(rejection.data);
      }
      else {
        alerts.addError({message: 'Unexpected server error'});
      }
      return $q.reject(rejection);
    }
  };
}).config(function($httpProvider) {
  $httpProvider.interceptors.push('errorInterceptor');
});
