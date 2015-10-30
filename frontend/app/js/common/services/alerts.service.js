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
(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.common.services')
    .factory('alerts', alertsService);

  function alertsService($interval, gettext) {

    var alerts = [];
    var defaultDelay = 8000;

    return {
      alerts: alerts,
      remove: remove,
      addError: addAlert.bind(null, gettext('error')),
      addSuccess: addAlert.bind(null, gettext('success')),
      addWarning: addAlert.bind(null, gettext('warning'))
    };

    /**
     * Adds an alert and removes it a few seconds later
     * @param error
     */
    function add(alert, delay) {
      alerts.push(alert);
      // we use $interval here instead of $timeout to be testable with protractor.
      // Protractor is waiting for $timeout to be over so for alert with delay > ptor timeout, test will fail. Moreover this slow down our test suite
      // Protractor is not waiting for $interval to be over so we make an interval being executed one time
      // see https://github.com/angular/protractor/issues/169
      $interval(function() {
        remove(0);
      }, delay || defaultDelay, 1);
    }

    /**
     * Removes the alert at the given index
     * @param index
     */
    function remove(index) {
      alerts.splice(index, 1);
    }

    /**
     * An alert could be a message or an object
     * @param alert
     */
    function getAlert(alert, type) {
      if (typeof alert === 'string') {
        return { type: type, content: alert };
      } else {
        alert.type = type;
        return alert;
      }
    }

    function addAlert(type, alert, delay) {
      var a = getAlert(alert, type);
      add(a, delay);
    }
  }

})();
