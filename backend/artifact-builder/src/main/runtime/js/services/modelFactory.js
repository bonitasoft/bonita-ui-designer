(function() {
  'use strict';

  function modelFactory($q, ResolverService) {

    return {
      create: function(data) {

        //the model will be filled via resolvers execution
        var model = {};

        //for each data, create the associated resolver from the data type
        var resolvers = Object.keys(data)
          .map((name) => ResolverService.createResolver(model, name, data[name]));
        //waiting for all data without dependencies to be resolved before
        //resolving data that have dependencies
        $q.all(resolvers
          .filter((resolver) => !resolver.hasDependencies())
          .map((resolver) => $q.when(resolver.resolve())))
          .finally(() => resolvers.forEach((resolver) => resolver.watchDependencies()));

        model.createGateway = function() {
          var context = {};
          Object.keys(data).forEach(function(property) {
            Object.defineProperty(context, property, {
              get: () => model[property],
              set: (value) => model[property] = value,
              enumerable: true
            });
          });
          return context;
        };
        return model;
      }
    };
  }

  angular.module('bonitasoft.ui.services')
    .factory('modelFactory', modelFactory);
})();
