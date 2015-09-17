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
(function () {

  'use strict';

  angular
    .module('bonitasoft.designer.services')
    .service('whiteboard', whiteboardService);

  function whiteboardService($q, widgetRepo, paletteService, componentFactory, commonParams, alerts, gettext) {

    var paletteItems = {};
    return {
      addPalette: addPalette,
      initialize: initialize
    };

    function addPalette(key, repository) {
      paletteItems[key] = repository;
    }

    function initialize(repo, id) {
      return widgetRepo.all()
        .then(initializePalette)
        .then(function () {
          var promises = Object.keys(paletteItems)
            .reduce(function (promises, key) {
              return promises.concat(paletteItems[key](id));
            }, []);
          return $q.all(promises);
        })
        .then(repo.load.bind(null, id))
        .catch(function (error) {
          alerts.addError(error.message);
          return $q.reject(error);
        })
        .then(function (response) {
          componentFactory.initializePage(response.data);
          return response.data;
        });
    }

    function initializePalette(response) {
      function filterCustoms(val, item) {
        return item.custom === val;
      }

      var widgets = response.data.map(function (widget) {
        widget.properties = commonParams.getDefinitions().concat(widget.properties || []);
        return widget;
      });

      var coreWidgets = widgets.filter(filterCustoms.bind(null, false))
        .map(componentFactory.paletteWrapper.bind(null, gettext('widgets'), 1));

      var customWidgets = widgets.filter(filterCustoms.bind(null, true))
        .map(componentFactory.paletteWrapper.bind(null, gettext('custom widgets'), 2));

      // reset the components map
      paletteService.reset();
      paletteService.register(componentFactory.getPaletteContainers());
      paletteService.register(coreWidgets);
      paletteService.register(customWidgets);
    }
  }

})();
