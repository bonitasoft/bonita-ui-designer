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
    .factory('dataManagementRepo', dataManagementRepo);

  function dataManagementRepo($http, $log) {

    class DataManagementRepo {
      constructor() {
        this.isError = false;
        this.baseUrl = './bdm/json';
        this.$http = $http;
        this.config = {
          headers: {
            'Content-Type': 'application/json',
          }
        };
        this.jsonResponse = null;
      }

      getDataObject(businessObjectName) {
        let response = this.jsonResponse.data;
        let businessObject = null;
        let resp = { error: false, businessObject: { name: businessObjectName, attributes: [] } };
        if (!response || !response.businessObjects || response.businessObjects.length === 0) {
          return resp;
        }
        let objs = response.businessObjects;
        for (let obj of objs) {
          if (obj.qualifiedName === businessObjectName) {
            businessObject = obj;
            break;
          }
        }
        if (!businessObject) {
          return resp;
        }

        resp.businessObject.attributes = this.getAttributes(objs, businessObjectName);
        return resp;
      }

      getAttributes(allObjects, businessObjectName) {
        let bo = Object.values(this._getBusinessObject(allObjects, businessObjectName))[0];
        bo.attributes.forEach(attr => {
          if (attr.reference) {
            attr.attributes = this.getAttributes(allObjects, attr.reference);
          }
        });
        return bo.attributes;
      }

      getDataObjects() {
        if (this.isError) {
          return new Promise((resolve) => resolve({ error: true, objects: [] }));
        }

        // Query the BDR to get BDM info
        return this.$http.get(`${this.baseUrl}`, this.config)
          .then((response) => {
            this.jsonResponse = response;
            let returnBusiness = [];
            if (!response || !response.data || !response.data.businessObjects) {
              return { error: false, objects: returnBusiness };
            }

            let businessObjects = response.data.businessObjects;
            businessObjects.forEach(bo => returnBusiness.push(
              {
                name: bo.name,
                id: bo.qualifiedName,
                description: bo.description
              }));
            return { error: false, objects: returnBusiness };

          })
          .catch((e) => {
            this.isError = true;
            $log.log('Error during loading of BusinessData objects', e);
            return { error: true, objects: [] };
          });
      }

      getQueries(businessObjectName) {
        if (this.isError) {
          return new Promise((resolve) => resolve({}));
        }
        let response = this.jsonResponse;

        let queries = { defaultQuery: [], additionalQuery: [] };
        if (!response || !response.data || !response.data.businessObjects) {
          return queries;
        }
        let businessObjects = response.data.businessObjects;
        let businessObject = businessObjects.filter(bo => bo.qualifiedName === businessObjectName);
        if (businessObject.length !== 1) {
          return queries;
        }
        businessObject = businessObject[0];
        if (businessObject.attributeQueries) {
          businessObject.attributeQueries.forEach(field => {
            this._pushIn(field, queries);
          });
        }
        if (businessObject.constraintQueries) {
          businessObject.constraintQueries.forEach(field => {
            this._pushInAdditional(field, queries.additionalQuery);
          });
        }

        if (businessObject.customQueries) {
          businessObject.customQueries.forEach(field => {
            this._pushInAdditional(field, queries.additionalQuery);
          });
        }
        return queries;
      }

      _pushIn(query, objects) {
        if (query.name === 'find') {
          objects.additionalQuery.push({
            displayName: query.name,
            query: query.name,
            filters: query.filters
          });
          return;
        }
        let displayName = query.name.split('findBy');
        if (displayName.length !== 2) {
          return;
        }
        objects.defaultQuery.push({
          displayName: displayName[1].replace(/\b\w/g, l => l.toLowerCase()),
          query: query.name,
          filters: query.filters
        });
      }

      _pushInAdditional(query, objects) {
        objects.push({
          displayName: query.name,
          query: query.name,
          filters: query.filters
        });
      }

      // For tests
      _setJsonResponse(resp) {
        this.jsonResponse = resp;
      }

      _getBusinessObject(allObjects, businessObjectName) {
        return allObjects.filter(bo => bo.qualifiedName === businessObjectName);
      }
    }

    return new DataManagementRepo();
  }

})();
