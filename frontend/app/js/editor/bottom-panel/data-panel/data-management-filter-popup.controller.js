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

  class DataManagementPopupController {
    constructor($uibModalInstance, businessData, queriesForObject, pageData) {
      this.$uibModalInstance = $uibModalInstance;
      this.businessData = businessData;
      this.queriesForObject = queriesForObject;
      this.pageData = pageData;
      this.newData = { $$name: this.generateVariableName(this.businessData.name) };
      this.variableInfo = {};
    }

    /**
     * Generate name with suffix when name isn't unique
     * @param name
     * @returns name_{Number}
     */
    generateVariableName(name) {
      let generateName = name.replace(/\b\w/g, l => l.toLowerCase());
      if (!this.isDataNameUnique(generateName)) {
        let index = 1;
        while (!this.isDataNameUnique(generateName.concat('_',index))) {
          index++;
        }
        generateName = generateName.concat('_',index);

        return generateName;
      }
      return generateName;
    }

    save() {
      this.pageData[this.newData.$$name] = {
        exposed: false,
        type: 'businessdata',
        displayValue: this.hasData() ? JSON.stringify(this.variableInfo.data) : '{}',
      };
      this.$uibModalInstance.close();
    }

    isDataNameUnique(dataName) {
      return !dataName || !this.pageData[dataName];
    }

    hasData() {
      return this.variableInfo.hasOwnProperty('data');
    }
  }

  angular.module('bonitasoft.designer.editor.bottom-panel.data-panel')
    .controller('DataManagementPopupController', DataManagementPopupController);
})();
