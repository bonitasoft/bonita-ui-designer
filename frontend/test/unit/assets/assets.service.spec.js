describe('utils', function () {
  var assets;

  beforeEach(module('pb.assets'));

  beforeEach(inject(function ($injector) {
    assets = $injector.get('assets');
  }));

  describe('getTypeLabel', function () {
    it('should return "" by default', function () {
      expect(assets.getTypeLabel()).toBe('');
    });

    it('should return "CSS" for type css', function () {
      expect(assets.getTypeLabel('css')).toBe('CSS');
    });

    it('should return "Images" for type img', function () {
      expect(assets.getTypeLabel('img')).toBe('Images');
    });

    it('should return "JavaScript" for type js', function () {
      expect(assets.getTypeLabel('js')).toBe('JavaScript');
    });
  });

  describe('data', function () {
    it('initFilterMap should return a table for all the types and a filter initialized to true', function () {
      expect(assets.initFilterMap()).toEqual(
        [
          { key: 'js', label: 'JavaScript', filter: true},
          { key: 'css', label: 'CSS', filter: true},
          { key: 'img', label: 'Images', filter: true}
        ]);
    });

    it('getPlaces should return a table containing Local and External', function () {
      expect(assets.getPlaces()).toEqual(['External', 'Local']);
    });

    it('getExternalPlace should return External', function () {
      expect(assets.getExternalPlace()).toBe('External');
    });

    it('getTypes should return a table with all the types ', function () {
      expect(assets.getTypes()).toEqual(
        [
          { key: 'js', label: 'JavaScript'},
          { key: 'css', label: 'CSS'},
          { key: 'img', label: 'Images'}
        ]);
    });
  });

  describe('assetToForm', function () {
    it('should return an object with default value for type and place', function () {
      expect(assets.assetToForm()).toEqual( {
        type : 'js',
        place : 'External'
      });
    });

    it('should return an object with external asset value ', function () {
      var asset = {
        name : 'http://asset.css',
        type : 'css'
      };
      expect(assets.assetToForm(asset)).toEqual( {
        name : 'http://asset.css',
        type : 'css',
        place : 'External'
      });
    });

    it('should return an object with local asset value ', function () {
      var asset = {
        name : 'asset.css',
        type : 'css'
      };
      expect(assets.assetToForm(asset)).toEqual( {
        name : 'asset.css',
        type : 'css',
        place : 'Local'
      });
    });
  });

  describe('formToAsset', function () {
    it('should return an asset', function () {
      var formasset = {
        name : 'http://asset.css',
        type : 'css',
        place : 'External'
      };
      expect(assets.formToAsset(formasset)).toEqual( {
        name : 'http://asset.css',
        type : 'css'
      });
    });
  });

});
