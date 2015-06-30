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
 * Repo to save or load a page.
 */
angular.module('pb.services')
  .service('whiteboard', function ($q, widgetRepo, paletteService, componentFactory, commonParams, alerts, gettext) {

    'use strict';

    var paletteItems = {};

    this.addPalette = function(key, repository) {
      paletteItems[key] = repository;
    };

    this.initialize = function(repo, id) {
      return widgetRepo.all()
        .then(initializePalette)
        .then( function() {
          var promises = Object.keys(paletteItems)
            .reduce(function(promises, key) {
              return promises.concat(paletteItems[key](id));
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

      // using gettext to add key to catalog that will be later translated in a template

      var coreWidgets = widgets.filter(isCoreWidget)
        .map(componentFactory.paletteWrapper.bind(null, gettext('widgets'), 1));

      var customWidgets = widgets.filter(isCustomWidget)
        .map(componentFactory.paletteWrapper.bind(null, gettext('customwidgets'), 2));
      // reset the components map
      paletteService.reset();
      paletteService.register(containerWidgets);
      paletteService.register(coreWidgets);
      paletteService.register(customWidgets);
    }

  });
