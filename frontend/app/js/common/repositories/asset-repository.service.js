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
(() => {
  'use strict';

  angular
    .module('bonitasoft.designer.common.repositories')
    .service('AssetRepository', assetRepository);

  function assetRepository($http) {

    class AssetRepository {
      constructor(baseUrl) {
        this.baseUrl = baseUrl;
        this.$http = $http;
      }

      createAsset(id, asset) {
        return this.$http.post(`${this.baseUrl}/${id}/assets`, asset)
          .then((response) => response.data);
      }

      deleteAsset(id, asset) {
        return this.$http.delete(`${this.baseUrl}/${id}/assets/${asset.id}`);
      }

      /**
       * Update a local asset content (i.e. replace file content by newContent)
       * @param component   component on which asset should be updated
       * @param asset       asset on which content should be updated
       * @param newContent  the new content
       * @returns {HttpPromise}
       */
      updateLocalAssetContent(componentId, asset, newContent) {
        var content = new Blob([newContent], { 'type': 'text/plain' });
        var formData = new FormData();
        formData.append('file', content, asset.name);

        return this.$http({
          url: `${this.baseUrl}/${componentId}/assets/${asset.type}`,
          method: 'POST',
          transformRequest: angular.identity,    // Do not serialize our FormData object
          data: formData,
          headers: {
            // Default angular content-type is application/json
            // Manually setting ‘Content-Type’: multipart/form-data will fail to fill in the boundary parameter of the request
            // We explicitly set content-type to undefined so browser will sets the Content-Type to multipart/form-data for us and fills in the correct boundary
            // See section 5.1.1. (Multipart Media Type / Common Syntax) of rfc2046 https://www.ietf.org/rfc/rfc2046.txt
            'Content-Type': undefined
          }
        }).then(response => response.data);
      }

      loadLocalAssetContent(componentId, asset) {
        return this.$http({
          url: `${this.baseUrl}/${componentId}/assets/${asset.type}/${asset.name}?format=text`,
          method: 'GET',
          transformResponse: angular.identity // Do not transform request, keep it in string even when this is a json content
        });
      }
    }
    return AssetRepository;
  }

})();
