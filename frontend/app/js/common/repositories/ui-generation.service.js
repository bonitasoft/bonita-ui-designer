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
    .factory('uiGeneration', uiGeneration);

  function uiGeneration($http, $log) {

    class UiGeneration {
      constructor(editorService) {
        this.editorService = editorService;
        this.isError = false;
        this.baseUrl = './rest/generation';
        this.$http = $http;
        this.config = {
          headers: {
            'Content-Type': 'application/json',
          }
        };
      }

      generateUI(businessObject, varName) {
        businessObject.variableName = varName;
        return this.$http.post(`${this.baseUrl}/businessobject`, businessObject)
          .then((response) => {
            if (!response || !response.data) {
              return { error: false, element: {} };
            }
            let resp = response.data;
            return { error: false, element: resp };
          })
          .catch((e) => {
            this.isError = true;
            $log.log('Error during UI generation', e);
            return { error: true, element: [] };
          });
      }
    }
    return new UiGeneration();
  }
})();
