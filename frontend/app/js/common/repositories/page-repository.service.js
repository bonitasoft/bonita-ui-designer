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
    .factory('pageRepo', pageRepository)
    .factory('formRepo', pageRepository)
    .factory('layoutRepo', pageRepository);

  function pageRepository(Repository) {

    class PageRepository extends Repository {
      constructor() {
        super('page', 'rest/pages');
      }
      /**
       * Renames a page and returns a promise
       * @param id - the page's id
       * @param newName - the page's new name
       */
      rename(id, newName) {
        return this.$http.put(`${this.baseUrl}/${id}/name`, newName);
      }

      /**
       * Creates a new asset
       * @param id - the page's id
       * @param asset
       */
      createAsset(id, asset) {
        return this.$http.post(`${this.baseUrl}/${id}/assets`, asset)
          .then((response) => response.data);
      }

      desactivateAsset(pageId, asset) {
        return this.$http.put(`${this.baseUrl}/${pageId}/assets/${asset.id}?active=${asset.active}`, asset);
      }

      /**
       * Loads assets used by the page and by the widgets
       * Returns a promise
       * @param page
       */
      loadAssets(page) {
        return this.$http.get(`${this.baseUrl}/${page.id}/assets`)
          .then((response) => response.data);
      }

      /**
       * Delete an asset
       * Returns a promise
       */
      deleteAsset(id, asset) {
        return this.$http.delete(`${this.baseUrl}/${id}/assets/${ asset.id}`);
      }
    }
    return new PageRepository();
  }
})();
