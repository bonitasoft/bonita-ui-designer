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
    .module('bonitasoft.designer.editor.common')
    .factory('properties', propertiesService);

  function propertiesService(gettext) {

    var commonProperties = [
      {
        label: gettext('CSS classes'),
        caption: gettext('Space-separated list'),
        name: 'cssClasses',
        type: 'string',
        defaultValue: '',
        bond: 'expression',
        help: gettext('Any accessible CSS classes. By default UI Designer comes with Bootstrap http://getbootstrap.com/css/#helper-classes')
      },
      {
        label: gettext('Hidden'),
        name: gettext('hidden'),
        type: 'boolean',
        defaultValue: false,
        bond: 'expression'
      }
    ];

    return {
      computeValues: computeValues,
      computeValue: computeValue,
      addCommonPropertiesTo: addCommonPropertiesTo,
      isBound
    };

    function computeValues(properties) {
      return (properties || []).reduce(function(props, property) {
        props[property.name] = computeValue(property);
        return props;
      }, {});
    }

    function computeValue(property) {
      return {
        type: property.bond === 'expression' ? 'constant' : property.bond,
        value: property.defaultValue
      };
    }

    function addCommonPropertiesTo(component) {
      component.properties = commonProperties.concat(component.properties || []);
      return component;
    }

    function isBound(propertyValue) {
      return propertyValue.type === 'expression' || propertyValue.type === 'variable';
    }
  }
})();
