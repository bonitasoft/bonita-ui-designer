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
angular.module('bonitasoft.ui.common.services').factory('alerts', function($timeout, gettext) {

  'use strict';

  var alerts = [];

  var defaultDelay = 8000;

  /**
   * Adds an alert and removes it a few seconds later
   * @param error
   */
  var add = function(alert, delay) {
    alerts.push(alert);
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

  /**
   * An alert could be a message or an object
   * @param alert
   */
  var getAlert = function(alert, type) {
    if (typeof alert === 'string') {
      return {type: type, content: alert};
    } else {
      alert.type = type;
      return alert;
    }
  };

  /**
   * Adds a success message and removes it a few seconds later
   * @param message
   */
  var addSuccess = function(alert, delay) {
    var a = getAlert(alert, gettext('success')); // gettext, add success to pot file
    add(a, delay);
  };

  /**
   * Adds an error and removes it a few seconds later
   * @param error
   */
  var addError = function(alert, delay) {
    var a = getAlert(alert, gettext('error')); // gettext, add error to pot file
    add(a, delay);
  };

  /**
   * Adds a warning message and removes it a few seconds later
   * @param error
   */
  var addWarning = function(alert, delay) {
    var a = getAlert(alert, gettext('warning')); // gettext, add warning to pot file
    add(a, delay);
  };

  return {
    alerts: alerts,
    addError: addError,
    addSuccess: addSuccess,
    remove: remove,
    addWarning: addWarning
  };
});
