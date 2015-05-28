describe('utils', function() {
  var assets;

  beforeEach(module('pb.assets'));

  beforeEach(inject(function($injector) {
    assets = $injector.get('assets');
  }));

  describe('getLabel', function() {
    it('should return "" by default', function() {
      expect(assets.getLabel()).toBe('');
    });

    it('should return "CSS" for type css', function() {
      expect(assets.getLabel('css')).toBe('CSS');
    });

    it('should return "Images" for type img', function() {
      expect(assets.getLabel('img')).toBe('Images');
    });

    it('should return "JavaScript" for type js', function() {
      expect(assets.getLabel('js')).toBe('JavaScript');
    });
  });

  describe('initFilterMap', function() {
    it('should return a table for all the types and a filter initialized to true', function () {
      expect(assets.initFilterMap()).toEqual(
        [
          { key: 'js', label: 'JavaScript', filter: true},
          { key: 'css', label: 'CSS', filter: true},
          { key: 'img', label: 'Images', filter: true}
      ]);
    });
  });

});
