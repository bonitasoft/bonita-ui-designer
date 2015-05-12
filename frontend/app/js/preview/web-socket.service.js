/* global SockJS */
/* global Stomp */
angular.module('pb.preview').factory('webSocket', function($rootScope, $log) {

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
