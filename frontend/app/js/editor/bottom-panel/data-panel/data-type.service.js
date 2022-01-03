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
angular.module('bonitasoft.designer.editor.bottom-panel.data-panel')
  .service('dataTypeService', function(gettext) {

    function compareType(type, item) {
      return type === item.type;
    }

    function getLabel(acc, item) {
      return item.label;
    }

    function getDefaultValue(acc, item) {
      return item.defaultValue;
    }

    let actionDataTypes = [
      { label: gettext('External API'), type: 'url', group: gettext('Generic'), defaultValue: '' },
      { label: gettext('Javascript expression'), type: 'expression', group: gettext('Generic'), defaultValue: 'return "hello";' },
      { label: gettext('Business data'), type: 'businessdata', group: gettext('Data Management'), defaultValue: '' },
    ];

    let variableDataTypes = [
      { label: gettext('String'), type: 'constant', group: gettext('Static'), defaultValue: '' },
      { label: gettext('JSON'), type: 'json', group: gettext('Static'), defaultValue: '{}' },
      { label: gettext('URL parameter'), type: 'urlparameter', group: gettext('Static'), defaultValue: '' },
    ];

    let dataTypes = variableDataTypes.concat(actionDataTypes);

    this.getVariableDataTypes = function() {
      return variableDataTypes;
    };

    this.getActionDataTypes = function() {
      return actionDataTypes;
    };

    this.getDataLabel = function(type) {
      return dataTypes
        .filter(compareType.bind(null, type))
        .reduce(getLabel, undefined);
    };

    this.getDataDefaultValue = function(type) {
      return dataTypes
        .filter(compareType.bind(null, type))
        .reduce(getDefaultValue, undefined);
    };

    this.save = function() {
      return {
        type: 'constant',
        exposed: false
      };
    };
  });
