describe('data-type', function() {
  let service;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.bottom-panel.data-panel'));
  beforeEach(inject(function($injector) {
    service = $injector.get('dataTypeService');
  }));

  describe('getDataTypes', function() {
    it('should return an object of dataTypes', function() {
      checkDataType(service.getVariableDataTypes());
      checkDataType(service.getActionDataTypes());
    });
  });

  describe('getDataLabel', function() {
    it('should return label for known types', function() {
      service.getVariableDataTypes().forEach(function(dataType) {
        expect(service.getDataLabel(dataType.type)).toEqual(dataType.label);
      });
      service.getActionDataTypes().forEach(function(dataType) {
        expect(service.getDataLabel(dataType.type)).toEqual(dataType.label);
      });
    });

    it('should return undefined for unknown types', function() {
      expect(service.getDataLabel('foo')).not.toBeDefined();
    });
  });

  describe('getDataDefaultValue', function() {
    it('should return label for known types', function() {
      service.getVariableDataTypes().forEach(function(dataType) {
        expect(service.getDataDefaultValue(dataType.type)).toEqual(dataType.defaultValue);
      });
      service.getActionDataTypes().forEach(function(dataType) {
        expect(service.getDataDefaultValue(dataType.type)).toEqual(dataType.defaultValue);
      });
    });

    it('should return undefined for unknown types', function() {
      expect(service.getDataDefaultValue('foo')).not.toBeDefined();
    });
  });

  describe('save', function() {
    it('should return a new object for data', function() {
      expect(service.save()).toBeDefined();
      expect(service.save().hasOwnProperty('type')).toBe(true);
      expect(service.save().hasOwnProperty('exposed')).toBe(true);
    });
  });

  function checkDataType(dataTypes) {
    let TYPES = ['constant', 'url', 'expression', 'json', 'urlparameter', 'businessdata'];
    expect(Array.isArray(dataTypes)).toBe(true);
    dataTypes.forEach(function (dataType) {
      expect(dataType.hasOwnProperty('type')).toBe(true);
      expect(TYPES.indexOf(dataType.type)).toBeGreaterThan(-1);
      expect(dataType.hasOwnProperty('label')).toBe(true);
      expect(dataType.hasOwnProperty('defaultValue')).toBe(true);
    });
  }

});
