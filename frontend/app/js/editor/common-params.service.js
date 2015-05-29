angular.module('pb.factories')
  .factory('commonParams', function() {

    'use strict';

    var common = [
      {
        label: 'CSS classes',
        caption: 'Space separated',
        name: 'cssClasses',
        type: 'string',
        defaultValue: ''
      },
      {
        label: 'Hidden',
        name: 'hidden',
        type: 'boolean',
        defaultValue: false
      }
    ];

    /**
     * Return custom params for a container or commons params for all
     * @return {Object}
     */
    function getDefinitions() {
      return common;
    }

    /**
     * Return custom properties as a container or global:
     * {
     *   type: 'constant',
     *   value: item.defaultValue
     * }
     *
     * @return {Object}
     */
    function getDefaultValues() {
      var propertyValues = {},
          data = getDefinitions();

      data.forEach(function (property) {
        propertyValues[property.name] = {
          type: 'constant',
          value: property.defaultValue
        };
      });

      return propertyValues;
    }

    return {
      getDefinitions: getDefinitions,
      getDefaultValues: getDefaultValues
    };

  });
