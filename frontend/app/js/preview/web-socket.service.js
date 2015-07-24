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
angular.module('bonitasoft.designer.preview').factory('webSocket', function($rootScope, $log) {

  'use strict';

  /**
   * Connects to a topic and calls the callback every time a message is received.
   * @param topic - the topic to connect to
   * @param callback - the callback that will be call when a message is received, with the message body
   */
  var listen = function(topic, callback) {
    var socket = new SockJS('websockets');
    var client = Stomp.over(socket);

    var subscribeCallback = function(message) {
      $rootScope.$apply(function() {
        callback(message.body);
      });
    };

    var subscribe = function() {
      client.subscribe(topic, subscribeCallback);
    };

    var error = function() {
      $log.error('error connecting to notifications web socket for topic ' + topic);
    };

    client.connect('', subscribe, error);
  };

  return {
    listen: listen
  };
});
