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
 * Repo containing all our palette widgets.
 */
angular.module('bonitasoft.designer.common.repositories').factory('widgetRepo', function($http) {

  'use strict';

  var lastSavedState = {};

  /**
   * Returns all the custom widgets by fetching all of them and filtering the custom ones.
   * @returns {*}
   */
  function customs() {
    return $http.get('rest/widgets?view=light')
      .then(function(response) {
        return response.data;
      })
      .then(function(widgets) {
        return widgets.filter(
          function(widget) {
            return widget.custom;
          });
      });
  }

  function all() {
    return $http.get('rest/widgets');
  }

  /**
   * Loads assets used by the widgets and by the widgets
   * Returns a promise
   * @param widget
   */
  function loadAssets(widget) {
    //we have to refresh the widget before
    return getById(widget.id).then(function(response) {
      return response.data.assets;
    });
  }

  function create(widget, sourceWidgetId) {
    return $http.post('rest/widgets' + (sourceWidgetId ? '?duplicata=' + sourceWidgetId : ''), widget).then(function(response) {
      return response.data;
    });
  }

  function save(widget) {
    return $http.put('rest/widgets/' + widget.id, widget)
      .success(function() {
       lastSavedState = widget;
     });
  }

  function createAsset(id, asset) {
    return $http.post('rest/widgets/' + id + '/assets', asset).then(function(response) {
      return response.data;
    });
  }

  function deleteAsset(id, asset) {
    //we need to send the object because the id should be an URL
    return $http.delete('rest/widgets/' + id + '/assets/' + asset.id);
  }

  function incrementOrderAsset(widgetId, asset) {
    return $http.put('rest/widgets/' + widgetId + '/assets/' + asset.id + '?increment=true', asset);
  }

  function decrementOrderAsset(widgetId, asset) {
    return $http.put('rest/widgets/' + widgetId + '/assets/' + asset.id + '?decrement=true', asset);
  }

  function remove(id) {
    return $http.delete('rest/widgets/' + id);
  }

  function getById(id) {
    return $http.get('rest/widgets/' + id);
  }

  function addProperty(widgetId, property) {
    return $http.post('rest/widgets/' + widgetId + '/properties', property);
  }

  function updateProperty(widgetId, propertyName, propertyUpdated) {
    return $http.put('rest/widgets/' + widgetId + '/properties/' + propertyName, propertyUpdated);
  }

  function deleteProperty(widgetId, propertyName) {
    return $http.delete('rest/widgets/' + widgetId + '/properties/' + propertyName);
  }

  /**
   * Return export url of a page
   * @param page - the page to be exported
   */
  var exportUrl = function(widget) {
    return 'export/widget/' + widget.id;
  };

  /**
   * Initialise lastSavedState to track update from editor
   * @param  {Object} widget  the current widget being edited
   */
  function initLastSavedState(widget) {
    lastSavedState = angular.copy(widget);
  }

  /**
   * Utility function to track if a widget being updated, need to be saved
   * @param  {Object} widget the widget being updated
   * @return {Boolean}
   */
  function needSave(widget) {
    return !angular.equals(widget, lastSavedState);
  }

  return {
    initLastSavedState: initLastSavedState,
    needSave: needSave,
    all: all,
    getById: getById,
    loadAssets: loadAssets,
    create: create,
    createAsset: createAsset,
    customs: customs,
    save: save,
    delete: remove,
    deleteAsset: deleteAsset,
    incrementOrderAsset: incrementOrderAsset,
    decrementOrderAsset: decrementOrderAsset,
    addProperty: addProperty,
    updateProperty: updateProperty,
    deleteProperty: deleteProperty,
    exportUrl: exportUrl,
    forceImport: (uuid) => $http.post('import/widget/' + uuid)
  };
});
