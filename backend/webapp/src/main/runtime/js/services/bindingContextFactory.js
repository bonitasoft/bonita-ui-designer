(function () {
  'use strict';

  function bindingContextFactory() {

    function expand(context) {
      return {
        "with": function (name, accessors) {
          accessors.enumerable = true;
          Object.defineProperty(context, name, accessors);
          return this;
        }
      };
    }

    function createContext(modelCtrl, scope) {
      var context = modelCtrl.createGateway();

      expand(context)

        .with('$item', {
          get: function () {
            return scope.$item;
          },
          set: function (value) {
            if (scope.$collection) {
              scope.$collection[scope.$index] = value;
            }
          }
        })

        .with('$collection', {
          get: function () {
            return scope.$collection;
          }
        })

        .with('$index', {
          get: function () {
            return scope.$index;
          }
        })

        .with('$form', {
          get: function () {
            return scope.$form;
          }
        });
      return context;
    }

    return {
      create: createContext
    }
  }

  angular
    .module('bonitasoft.ui.services')
    .factory('bindingContextFactory', bindingContextFactory);
})();
