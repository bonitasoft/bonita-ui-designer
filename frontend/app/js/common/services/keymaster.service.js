(function(){
  'use strict';
  /**
   * This modules wrap the keymaster library (available in window.key)
   * into an angular service
   */
  angular.module('pb.common.services').service('keymaster', function($window){
    // key is library name taht handle keyboard shortcuts
    return $window.key;
  });
})();
