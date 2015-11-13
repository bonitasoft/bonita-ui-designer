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
(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.common.repositories')
    .factory('widgetRepo', widgetRepository);

  function widgetRepository($http, repositories) {

    var repository = repositories.create('widget', 'rest/widgets');
    repository.customs = customs;
    repository.loadAssets = loadAssets;
    repository.createAsset = createAsset;
    repository.deleteAsset = deleteAsset;
    repository.incrementOrderAsset = incrementOrderAsset;
    repository.decrementOrderAsset = decrementOrderAsset;
    repository.addProperty = addProperty;
    repository.updateProperty = updateProperty;
    repository.deleteProperty = deleteProperty;
    return repository;

    /**
     * Returns all the custom widgets by fetching all of them and filtering the custom ones.
     * @returns {*}
     */
    function customs() {
      return $http.get(`${repository.baseUrl}?view=light`)
        .then((response) => response.data.filter((widget) => widget.custom));
    }

    /**
     * Loads assets used by the widgets and by the widgets
     * Returns a promise
     * @param widget
     */
    function loadAssets(widget) {
      return repository.load(widget.id)
        .then((response) => response.data.assets);
    }

    function createAsset(id, asset) {
      return $http.post(`${repository.baseUrl}/${id}/assets`, asset)
        .then((response) => response.data);
    }

    function deleteAsset(id, asset) {
      return $http.delete(`${repository.baseUrl}/${id}/assets/${asset.id}`);
    }

    function incrementOrderAsset(widgetId, asset) {
      return $http.put(`${repository.baseUrl}/${widgetId}/assets/${asset.id}?increment=true`, asset);
    }

    function decrementOrderAsset(widgetId, asset) {
      return $http.put(`${repository.baseUrl}/${widgetId}/assets/${asset.id}?decrement=true`, asset);
    }

    function addProperty(widgetId, property) {
      return $http.post(`${repository.baseUrl}/${widgetId}/properties`, property);
    }

    function updateProperty(widgetId, propertyName, propertyUpdated) {
      return $http.put(`${repository.baseUrl}/${widgetId}/properties/${propertyName}`, propertyUpdated);
    }

    function deleteProperty(widgetId, propertyName) {
      return $http.delete(`${repository.baseUrl}/${widgetId}/properties/${propertyName}`);
    }
  }

})();
