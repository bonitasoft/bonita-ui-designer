angular.module('pb.common.services').factory('alerts', function($timeout) {

  'use strict';


  var alerts = [];

  var defaultDelay = 8000;

  /**
   * Adds an error and removes it a few seconds later
   * @param error
   */
  var addError = function(error) {
    alerts.push({type: 'danger', message: error.message});
    $timeout(function() {
      remove(0);
    }, defaultDelay);
  };

  /**
   * Adds a success message and removes it a few seconds later
   * @param error
   */
  var addSuccess = function(message, delay) {
    alerts.push({type: 'success', message: message});
    $timeout(function() {
      remove(0);
    }, delay || defaultDelay);
  };

  /**
   * Removes the alert at the given index
   * @param index
   */
  var remove = function(index) {
    alerts.splice(index, 1);
  };

  return {
    alerts: alerts,
    addError: addError,
    addSuccess: addSuccess,
    remove: remove
  };
});
