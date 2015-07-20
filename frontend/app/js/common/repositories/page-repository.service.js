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
angular.module('pb.common.repositories').factory('pageRepo', function($http) {

  'use strict';

  var lastSavedState = {};

  /**
   * Lists all the pages and returns a promise containing the pages
   */
  function all() {
    return $http.get('rest/pages').then(function(response) {
      return response.data;
    });
  }

  /**
   * Creates a new page and returns a promise containing the returned data
   * @param content - the page's content (name and empty row, typically)
   * @param sourcePageId - for a save as this arg is the id of the source page
   */
  function create(content, sourcePageId) {
    return $http.post('rest/pages' + (sourcePageId ? '?duplicata=' + sourcePageId : ''), content).then(function(response) {
      return response.data;
    });
  }

  /**
   * Saves a page and returns a promise
   * @param id - the page's id
   * @param content - the page's content
   */
  function save(id, content) {
    return $http.put('rest/pages/' + id, content)
      .success(function() {
        lastSavedState = content;
      });
  }

  /**
   * Creates a new asset
   * @param id - the page's id
   * @param asset
   */
  function createAsset(id, asset) {
    return $http.post('rest/pages/' + id + '/assets', asset).then(function(response) {
      return response.data;
    });
  }

  /**
   * Renames a page and returns a promise
   * @param id - the page's id
   * @param newName - the page's new name
   */
  function rename(id, newName) {
    return $http.put('rest/pages/' + id + '/name', newName);
  }

  /**
   * Delete a page and returns a promise
   * @param id - the page's id
   */
  function deletePage(id) {
    return $http.delete('rest/pages/' + id);
  }

  function desactivateAsset(pageId, asset) {
    return $http.put('rest/pages/' + pageId + '/assets/' + asset.id + '?active=' + asset.active, asset);
  }

  /**
   * Loads the page identified by the given id
   * Returns a promise
   * @param id - the page's id
   */
  function load(id) {
    return $http.get('rest/pages/' + id);
  }

  /**
   * Loads assets used by the page and by the widgets
   * Returns a promise
   * @param page
   */
  function loadAssets(page) {
    return $http.get('rest/pages/' + page.id + '/assets').then(function(response) {
      return response.data;
    });
  }

  /**
   * Delete an asset
   * Returns a promise
   */
  function deleteAsset(id, asset) {
    return $http.delete('rest/pages/' + id + '/assets/' + asset.id);
  }

  /**
   * Return export url of a page
   * @param page - the page to be exported
   */
  var exportUrl = function(page) {
    return 'export/page/' + page.id;
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
    create: create,
    createAsset: createAsset,
    desactivateAsset: desactivateAsset,
    save: save,
    rename: rename,
    delete: deletePage,
    deleteAsset: deleteAsset,
    load: load,
    loadAssets: loadAssets,
    exportUrl: exportUrl
  };
});
