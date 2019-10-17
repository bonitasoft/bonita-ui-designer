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

    class SubsetBusinessDataController {

      constructor() {
        this.searchedQuery = '';
        this.advanced = false;
        this.advancedFilter = true;
        if (!this.variableInfo.pagination) {
          this.variableInfo.pagination = { p: 0, c: 10 };
        }
        if (this.variableInfo.filters) {
          this.variableInfo.data = this.evaluateVariable();
        }
      }

      getQueries(queries, search) {
        if (queries) {
          return queries.filter(query => query.displayName.toLowerCase().includes(search.toLowerCase()));
        }
      }

      displayFilters(querySelected, isAdditionnal) {
        let queries = this.queriesForObject.defaultQuery;
        if (isAdditionnal) {
          queries = this.queriesForObject.additionalQuery;
        }
        this.variableInfo.query = { name: querySelected.query, displayName: querySelected.displayName };

        let selected = queries.filter(query => query.displayName === querySelected.displayName);
        let filters = selected[0] ? selected[0].filters : [];
        let tmpFilters = [];
        filters.forEach(filter => {
          tmpFilters.push({ 'type': filter.type, 'name': filter.name, 'value': '' });
        });
        this.variableInfo.filters = tmpFilters;
        this.variableInfo.data = this.evaluateVariable();
      }

      evaluateVariable() {
        let filters = '';
        this.variableInfo.filters.forEach(filter => {
          filters = filters + '&f=' + filter.name.concat('=', filter.value);
        });

        let value = {
          displayValue: `${this.variableInfo.query.name} [${this.businessData.qualifiedName}]`,
          businessObjectName: `${this.businessData.name}`,
          query: this.variableInfo.query,
          id: this.businessData.id,
          qualifiedName: this.businessData.qualifiedName,
          filters: this.variableInfo.filters,
          pagination: this.variableInfo.pagination
        };

        return value;
      }

      getPrimitiveType(type) {
        return type === 'Int' ? 'Integer' : type;
      }
    }

    angular.module('bonitasoft.designer.editor.bottom-panel.data-panel').directive('subsetBusinessData', () => ({
      restrict: 'E',
      scope: {},
      bindToController: {
        businessData: '=',
        queriesForObject: '=',
        variableInfo: '=',
        editBusinessDataQueries: '='
      },
      templateUrl: 'js/editor/bottom-panel/data-panel/subset-businessData.html',
      controller: SubsetBusinessDataController,
      controllerAs: 'ctrl'
    }));
  }()
);

