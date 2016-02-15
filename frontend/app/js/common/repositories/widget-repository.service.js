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

  function widgetRepository(Repository) {

    class WidgetRepository extends Repository {
      constructor() {
        super('widget', 'rest/widgets');
      }
      /**
       * Returns all the custom widgets by fetching all of them and filtering the custom ones.
       * @returns {*}
       */
      customs() {
        return this.$http.get(`${this.baseUrl}?view=light`)
          .then((response) => response.data.filter((widget) => widget.custom))
          .then((widgets) => widgets.map((widget) => {
            widget.icon = true;
            widget.type = 'widget';
            return widget;
          }));
      }

      /**
       * Loads assets used by the widgets and by the widgets
       * Returns a promise
       * @param widget
       */
      loadAssets(widget) {
        return this.load(widget.id)
          .then((response) => response.data.assets);
      }

      createAsset(id, asset) {
        return this.$http.post(`${this.baseUrl}/${id}/assets`, asset)
          .then((response) => response.data);
      }

      deleteAsset(id, asset) {
        return this.$http.delete(`${this.baseUrl}/${id}/assets/${asset.id}`);
      }

      incrementOrderAsset(widgetId, asset) {
        return this.$http.put(`${this.baseUrl}/${widgetId}/assets/${asset.id}?increment=true`, asset);
      }

      decrementOrderAsset(widgetId, asset) {
        return this.$http.put(`${this.baseUrl}/${widgetId}/assets/${asset.id}?decrement=true`, asset);
      }

      addProperty(widgetId, property) {
        return this.$http.post(`${this.baseUrl}/${widgetId}/properties`, property);
      }

      updateProperty(widgetId, propertyName, propertyUpdated) {
        return this.$http.put(`${this.baseUrl}/${widgetId}/properties/${propertyName}`, propertyUpdated);
      }

      deleteProperty(widgetId, propertyName) {
        return this.$http.delete(`${this.baseUrl}/${widgetId}/properties/${propertyName}`);
      }
    }
    return new WidgetRepository();
  }
})();
