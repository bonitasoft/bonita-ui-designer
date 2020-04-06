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

  class BusinessDataUpdate {

    constructor(businessData, variableInfo) {
      this.businessData = businessData;
      this.variableInfo = variableInfo;
      if (!this.variableInfo || !this.variableInfo.hasOwnProperty('pagination')) {
        this.variableInfo = {};
        this.variableInfo.pagination = { p: 0, c: 10 };
      }
    }

    queryChanged(e) {
      let query = e.detail.query;
      this.variableInfo.query = { name: query, displayName: query };
      this.variableInfo.filters = e.detail.filters;
      this.variableInfo.pagination.p = e.detail.pagination.pageIndex;
      this.variableInfo.pagination.c = e.detail.pagination.nbElements;
      this.variableInfo.data = this.getDataManagementObject();
      return this.variableInfo;
    }

    isDataValid(e) {
      return e.detail.validity;
    }

    getDataManagementObject() {
      return {
        displayValue: `${this.variableInfo.query.name} [${this.businessData.id}]`,
        businessObjectName: `${this.businessData.name}`,
        query: this.variableInfo.query,
        id: this.businessData.id,
        filters: this.variableInfo.filters,
        pagination: this.variableInfo.pagination
      };
    }

    hasData() {
      return this.variableInfo.hasOwnProperty('data');
    }

  }

  angular.module('bonitasoft.designer.editor.bottom-panel.data-panel').service('businessDataUpdateService', () => ({
    create(businessData, variableInfo) {
      return new BusinessDataUpdate(businessData, variableInfo);
    }
  }));
}());
