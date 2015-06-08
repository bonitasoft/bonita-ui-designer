/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
