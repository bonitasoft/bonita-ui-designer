angular.module('pb.services')
  .service('dataTypeService', function (gettext){

    function compareType(type, item) {
      return type === item.type;
    }

    function getLabel(acc, item) {
      return item.label;
    }

    function getDefaultValue(acc, item) {
      return item.defaultValue;
    }
    var dataTypes = [
      { label: gettext('String'), type: 'constant', group: ' ', defaultValue: ''},
      { label: gettext('JSON'), type: 'json', group: ' ', defaultValue: '{}'},
      { label: gettext('External API'), type: 'url', group: '--', defaultValue: ''},
      { label: gettext('Expression'), type: 'expression', group: '--', defaultValue: 'return "hello";'},
      { label: gettext('URL parameter'), type: 'urlparameter', group: '--', defaultValue: ''},
    ];

    this.getDataTypes = function() {
      return dataTypes;
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

    this.createData = function() {
      return {
        type: 'constant',
        exposed: false
      };
    };
});
