angular.module('pb.common.services').factory('clock', function() {

  'use strict';

  return {
    /**
     * Returns the current time (Date.now()). Useful as it can be mocked in tests.
     */
    now: Date.now
  };
});
