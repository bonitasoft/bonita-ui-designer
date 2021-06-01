(function() {
  'use strict';

  function localStorageService($window) {

    return {
      save: storeInLocalStorage,
      read: readFromLocalStorage,
      delete: removeFromLocalStorage,
      isAvailable: checkLocalStorageAvailability
    };

    /**
    **  Save the given value as a string in the local storage.
    **  The value can be an object, a string or any type supported by JSON.stringify().
    **  As a result the actual ID used to persist the value is returned. This is only for information as
    **  this ID is never used as an input of any method of the localStorageService.
    **/
    function storeInLocalStorage(url, value) {
      var id = generateIdFromURL(url);
      $window.localStorage.setItem(id, JSON.stringify(value));
      return id;
    }


    function generateIdFromURL(url) {
      return 'bonita-form-' + removeCacheBustingParameter(url);
    }

    function removeCacheBustingParameter(url) {

      // JS trick to parse string URL
      var parser = document.createElement('a');
      parser.href = url;

      var queries, parameters;
      queries = parser.search.replace(/^\?/, '').split('&');

      var ignoreTimeParameter = function(acc, current) {
        if(!current.startsWith('time=')) {
          var separator='';
          if (acc.length >0) {
            separator = '&';
          }
          return acc + separator + current;
        }
        // ignore cache busting parameter
        return acc;
      };

      parameters = queries.reduce(ignoreTimeParameter, '');

      // Replace search query with clean value.
      parser.search = null;
      if(parameters.length > 0) {
        parser.search = parameters;
      }

      return parser.href;

    }

    function readFromLocalStorage(url) {
      var cachedValue = $window.localStorage.getItem(generateIdFromURL(url));
      return JSON.parse(cachedValue);
    }

    function removeFromLocalStorage(url) {
      $window.localStorage.removeItem(generateIdFromURL(url));
    }

    function checkLocalStorageAvailability() {
      return !!$window.localStorage;
    }

  }
  angular.module('bonitasoft.ui.services')
    .service('localStorageService', localStorageService);
})();
