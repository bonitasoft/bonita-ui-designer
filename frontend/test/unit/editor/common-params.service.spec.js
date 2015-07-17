describe('factory commonParams', function() {

  var commonParams;

  beforeEach(module('bonitasoft.ui.factories', 'gettext'));
  beforeEach(inject(function ($injector) {
    commonParams = $injector.get('commonParams');
  }));

  describe('We can work with common configuration', function() {

    it('should list the common properties', function() {
      var properties = commonParams.getDefinitions();

      expect(properties.map(function(property) {
        return property.name;
      })).toEqual(['cssClasses', 'hidden']);
    });

    it('should list each properties with a type constant and value=defaultValue', function() {
      var properties = commonParams.getDefaultValues();
      expect(properties.cssClasses.type).toBe('constant');
      expect(properties.hidden.type).toBe('constant');

      expect(properties.cssClasses.value).toBe('');
      expect(properties.hidden.value).toBe(false);
    });

  });
});
