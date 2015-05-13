/**
 * Repo to save or load a page.
 */
angular.module('pb.services')
  .service('whiteboard', function ($q, widgetRepo, paletteService, componentFactory, commonParams, alerts) {

    'use strict';

    var paletteItems = {};

    this.addPalette = function(key, repository) {
      paletteItems[key] = repository;
    };

    this.initialize = function(repo, id ) {
      return widgetRepo.all()
        .then(initializePalette)
        .then( function() {
          var promises = Object.keys(paletteItems)
            .reduce(function(promises, key) {
              return promises.concat(paletteItems[key]());
            }, []);
          return $q.all( promises );
        })
        .then( repo.load.bind(null, id) )
        .catch(function(error){
          alerts.addError(error);
          return $q.reject(error);
        })
        .then( function(response) {
          componentFactory.initializePage(response.data);
          return response.data;
        });
    };

    function initializePalette(response) {
      function filterCustoms(val, item) {
        return item.custom === val;
      }
      var widgets = response.data.map(function(widget) {
        widget.properties = commonParams.getDefinitions().concat(widget.properties || []);
        return widget;
      });

      var isCoreWidget = filterCustoms.bind(null, false);
      var isCustomWidget = filterCustoms.bind(null, true);

      var containerWidgets = componentFactory.getPaletteContainers();


      var coreWidgets = widgets.filter(isCoreWidget)
        .map(componentFactory.paletteWrapper.bind(null, 'widget', 1));

      var customWidgets = widgets.filter(isCustomWidget)
        .map(componentFactory.paletteWrapper.bind(null, 'customWidgets', 2));
      // reset the components map
      paletteService.reset();
      paletteService.register(containerWidgets);
      paletteService.register(coreWidgets);
      paletteService.register(customWidgets);
    }

  });
