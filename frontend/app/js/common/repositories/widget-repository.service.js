/**
 * Repo containing all our palette widgets.
 */
angular.module('pb.common.repositories').factory('widgetRepo', function($http, $q) {

  'use strict';

  /**
   * Returns all the custom widgets by fetching all of them and filtering the custom ones.
   * @returns {*}
   */
  function customs() {
    return $http.get('rest/widgets?view=light')
      .then(function (response) {
        return response.data;
      })
      .then(function (widgets) {
        return widgets.filter(
          function (widget) {
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
    return $q.when({
      data : widget.assets
    });
  }

  function create(widget) {
    return $http.post('rest/widgets', widget).then(function (response) {
      return response.data;
    });
  }

  function save(widget) {
    return $http.put('rest/widgets/' + widget.id, widget);
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

  return {
    all: all,
    getById: getById,
    loadAssets: loadAssets,
    create: create,
    customs: customs,
    save: save,
    delete: remove,
    addProperty: addProperty,
    updateProperty: updateProperty,
    deleteProperty: deleteProperty,
    exportUrl:exportUrl
  };
});
