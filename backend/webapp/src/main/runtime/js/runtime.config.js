(function () {

  'use strict';

  angular.module('bonitasoft.ui')
    .config(function ($httpProvider) {
      $httpProvider.interceptors.push('httpActivityInterceptor');
    });

})();
