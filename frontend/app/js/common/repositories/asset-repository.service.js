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
    }
    return AssetRepository;
  }

})();
