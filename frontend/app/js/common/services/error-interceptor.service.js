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
/**
 * http response interceptor which extracts the error message from an error http response and adds it to the alerts
 * service
 */
angular.module('bonitasoft.designer.common.services').factory('errorInterceptor', function($q, alerts) {

  'use strict';

  return {
    responseError: function(rejection) {
      if (rejection.headers('Content-Type') &&
          rejection.headers('Content-Type').indexOf('application/json') === 0 &&
          angular.isDefined(rejection.data.message)) {
        alerts.addError(rejection.data.message);
      } else {
        alerts.addError('Unexpected server error');
      }
      return $q.reject(rejection);
    }
  };
}).config(function($httpProvider) {
  $httpProvider.interceptors.push('errorInterceptor');
});
