(function() {
  'use strict';
  angular.module('pb.common.services').service('utils', function() {

    /**
     * clamp a value between a min and a max value
     * @param  {number} min
     * @param  {number} value
     * @param  {number} max
     * @return {number}       the clamp value
     */
    this.clamp = function clam(min, value, max) {
      return value < min ? min : (value > max ? max : value);
    };
  });
})();
