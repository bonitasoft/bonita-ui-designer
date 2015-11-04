describe('data-type', function() {
  var service;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.data-panel'));
  beforeEach(inject(function($injector) {
    service = $injector.get('dataTypeService');
  }));

  describe('getDataTypes', function() {
    it('should return an object of dataTypes', function() {
      var dataTypes = service.getDataTypes();
      var TYPES = ['constant', 'url', 'expression', 'json', 'urlparameter'];
      expect(Array.isArray(dataTypes)).toBe(true);
      dataTypes.forEach(function(dataType) {
        expect(dataType.hasOwnProperty('type')).toBe(true);
        expect(TYPES.indexOf(dataType.type)).toBeGreaterThan(-1);
        expect(dataType.hasOwnProperty('label')).toBe(true);
        expect(dataType.hasOwnProperty('defaultValue')).toBe(true);
      });
    });
  });

  describe('getDataLabel', function() {
    it('should return label for known types', function() {
      service.getDataTypes().forEach(function(dataType) {
        expect(service.getDataLabel(dataType.type)).toEqual(dataType.label);
      });
    });

    it('should return undefined for unknown types', function() {
      expect(service.getDataLabel('foo')).not.toBeDefined();
    });
  });

  describe('getDataDefaultValue', function() {
    it('should return label for known types', function() {
      service.getDataTypes().forEach(function(dataType) {
        expect(service.getDataDefaultValue(dataType.type)).toEqual(dataType.defaultValue);
      });
    });

    it('should return undefined for unknown types', function() {
      expect(service.getDataDefaultValue('foo')).not.toBeDefined();
    });
  });

  describe('createData', function() {
    it('should return a new object for data', function() {
      expect(service.createData()).toBeDefined();
      expect(service.createData().hasOwnProperty('type')).toBe(true);
      expect(service.createData().hasOwnProperty('exposed')).toBe(true);
    });
  });

});
