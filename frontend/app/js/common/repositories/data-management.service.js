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
        this.baseUrl = './bdr';
        this.$http = $http;
        this.config = {
          headers: {
            'Content-Type': 'application/json',
          }
        };
      }

      getDataObject(businessObject) {
        let businessObjectReq = `{ __type(name: "${businessObject}") {
                        name
                        fields {
                          name
                          type { name, kind }
                        }}}`;
        return this.$http.post(`${this.baseUrl}`, { 'query': businessObjectReq }, this.config)
          .then((response) => {
            let resp = { error: false, businessObject: { name: businessObject } };
            if (!response || !response.data || !response.data.data || !response.data.data.__type || !response.data.data.__type.fields) {
              resp.businessObject.attributes = [];
              return resp;
            }
            resp.businessObject.attributes = response.data.data.__type.fields;
            return resp;
          }).catch((e) => {
            $log.log('Error during loading of attributes of BusinessData object' + businessObject, e);
            return { error: true, businessObject: { name: businessObject, attributes: [] } };
          });
      }

      getDataObjects() {
        if (this.isError) {
          return new Promise((resolve) => resolve({ error: true, objects: [] }));
        }

        let query = `{ __schema{ types { name,kind,description } } }`;
        return this.$http.post(`${this.baseUrl}`, { 'query': query }, this.config)
          .then((response) => {
            let returnBusiness = [];
            if (!response || !response.data || !response.data.data || !response.data.data.__schema || !response.data.data.__schema.types) {
              return { error: false, objects: returnBusiness };
            }

            let businessObject = response.data.data.__schema.types;
            let boFilters = businessObject.filter(obj => this._isBusinessObject(obj));
            boFilters.forEach(bo => returnBusiness.push(
              {
                qualifiedName: bo.name.split('_').join('.'),
                name: bo.name.split('_').pop(),
                id: bo.name,
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

      getQueries(businessObject) {
        if (this.isError) {
          return new Promise((resolve) => resolve({}));
        }

        let query = `{queriesAttributeQuery: __type(name: "${businessObject}AttributeQuery") { ...typeFields }
                      queriesContraintQuery: __type(name: "${businessObject}ConstraintQuery") { ...typeFields }
                      queriesCustomQuery: __type(name: "${businessObject}CustomQuery") {  ...typeFields}}
                      fragment typeFields on __Type { fields { name args { name, type { ofType{ name } } } type { name } }}`;
        return this.$http.post(`${this.baseUrl}`, { 'query': query }, this.config)
          .then((response) => {
            let queries = { defaultQuery: [], additionalQuery: [] };
            if (!response || !response.data || !response.data.data) {
              return queries;
            }
            let data = response.data.data;
            if (data.queriesAttributeQuery && data.queriesAttributeQuery.fields) {
              response.data.data.queriesAttributeQuery.fields.forEach(field => {
                this._pushIn(field, queries);
              });
            }
            if (data.queriesContraintQuery && data.queriesContraintQuery.fields) {
              response.data.data.queriesContraintQuery.fields.forEach(field => {
                this._pushInAdditional(field, queries.additionalQuery);
              });
            }

            if (data.queriesCustomQuery && data.queriesCustomQuery.fields) {
              response.data.data.queriesCustomQuery.fields.forEach(field => {
                this._pushInAdditional(field, queries.additionalQuery);
              });
            }
            return queries;
          })
          .catch((e) => {
            $log.log('Error during loading of BusinessData queries', e);
            return [];
          });
      }

      _isBusinessObject(businessObject) {
        return !businessObject.name.startsWith('__') && businessObject.kind === 'OBJECT' && !businessObject.name.endsWith('Query');
      }

      _pushIn(query, objects) {
        if (query.name === 'find') {
          objects.additionalQuery.push({
            displayName: query.name,
            query: query.name,
            filters: this._createFilters(query.args)
          });
          return;
        }
        let displayName = query.name.split('findBy');
        if (displayName.length > 1) {
          displayName.shift();
        }
        objects.defaultQuery.push({
          displayName: displayName.join('').replace(/\b\w/g, l => l.toLowerCase()),
          query: query.name,
          filters: this._createFilters(query.args)
        });
      }

      _pushInAdditional(query, objects) {
        objects.push({
          displayName: query.name,
          query: query.name,
          filters: this._createFilters(query.args)
        });
      }

      _createFilters(args) {
        let res = [];
        args.forEach(arg => {
          res.push({ name: arg.name, type: arg.type.ofType.name });
        });
        return res;
      }
    }

    return new DataManagementRepo();
  }

})();
