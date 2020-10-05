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

  function dataManagementRepo($http, $log, alerts, gettextCatalog) {

    class DataManagementRepo {
      constructor() {
        this.isError = false;
        this.baseUrl = './bdm/json';
        this.$http = $http;
        this.maxDepth = 5;
        this.allBusinessObjects = [];
        this.config = {
          headers: {
            'Content-Type': 'application/json',
          }
        };
        this.jsonResponse = null;
      }

      getDataObject(businessObjectName) {

        let response = this.jsonResponse.data;
        this.allBusinessObjects = response.businessObjects;
        let businessObject = null;
        let resp = { error: false, businessObject: { name: businessObjectName, attributes: [] } };
        if (!response || !response.businessObjects || response.businessObjects.length === 0) {
          return resp;
        }

        for (let obj of this.allBusinessObjects) {
          if (obj.qualifiedName === businessObjectName) {
            businessObject = obj;
            break;
          }
        }

        if (!businessObject) {
          return resp;
        }

        let bo = Object.values(this._getBusinessObject(this.allBusinessObjects, businessObjectName))[0];
        // Decrement max level relation
        resp.businessObject.attributes = this.buildAttributes(bo, this.maxDepth);
        return resp;
      }

      buildAttributes(businessObject, depth) {
        if (depth > 0) {
          let attributes = [];
          businessObject.attributes
            .map(attr => this.createAttributeFromBo(attr, depth))
            .forEach(createdAttr => attributes.push(Object.assign({}, createdAttr)));
          return attributes;
        } else {
          alerts.addInfo(gettextCatalog.getString('This Business Object contains more than {{number}} levels of relations. {{numberAsText}} level and beyond will not appear.', { number: this.maxDepth, numberAsText: 'Fifth' }), 5000);
        }
      }

      createAttributeFromBo(attr, depth) {
        // Relation business Object
        if (attr.reference) {
          let relationFields = this.createRelationField(attr, depth);
          attr.ref = Math.random();
          attr.attributes = Object.assign([], relationFields);
        }
        return Object.assign({}, attr);
      }

      createRelationField(attr, depth = 5) {
        // Decrement depth
        if (depth > 0) {
          depth = depth - 1;
          let bo = Object.values(this._getBusinessObject(this.allBusinessObjects, attr.reference))[0];
          return this.buildAttributes(bo, depth);
        }
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

        queries.defaultQuery = queries.defaultQuery.sort(this._sortByProperty('displayName'));
        queries.additionalQuery = queries.additionalQuery.sort(this._sortByProperty('displayName'));
        return queries;
      }

      _sortByProperty(property) {
        return function(a, b) {
          if (a[property] > b[property]) {
            return 1;
          } else if (a[property] < b[property]) {
            return -1;
          }
          return 0;
        };
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

    return new DataManagementRepo(alerts,gettextCatalog);
  }

})();
