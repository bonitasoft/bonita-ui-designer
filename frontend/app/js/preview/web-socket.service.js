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
/* global SockJS */
/* global Stomp */
angular.module('bonitasoft.designer.preview').factory('webSocket', function($rootScope, $q) {

  'use strict';
  var socket = new SockJS(window.location.origin + window.location.pathname + '/websockets');
  var client = Stomp.over(socket);
  client.debug = null; // deactivate debug mode

  /**
   * Connects to a topic and calls the callback every time a message is received.
   * @param topic - the topic to connect to
   * @param callback - the callback that will be call when a message is received, with the message body
   */
  var subscribe = function(topic, callback) {
    var subscribeCallback = function(message) {
      $rootScope.$apply(function() {
        callback(message.body);
      });
    };
    client.subscribe(topic, subscribeCallback);
  };

  var connect = function() {
    let deferred = $q.defer();
    client.connect({}, (frame) => deferred.resolve(frame), (err) => deferred.reject(err));
    return deferred.promise;
  };

  return {
    connect: connect,
    subscribe: subscribe
  };
});
