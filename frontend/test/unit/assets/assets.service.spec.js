describe('utils', function () {
  var assetsService;

  beforeEach(module('pb.assets'));

  beforeEach(inject(function ($injector) {
    assetsService = $injector.get('assetsService');
  }));

  describe('getTypeLabel', function () {
    it('should return "" by default', function () {
      expect(assetsService.getTypeLabel()).toBe('');
    });

    it('should return "CSS" for type css', function () {
      expect(assetsService.getTypeLabel('css')).toBe('CSS');
    });

    it('should return "Images" for type img', function () {
      expect(assetsService.getTypeLabel('img')).toBe('Images');
    });

    it('should return "JavaScript" for type js', function () {
      expect(assetsService.getTypeLabel('js')).toBe('JavaScript');
    });
  });

  describe('data', function () {
    it('initFilterMap should return a table for all the types and a filter initialized to true', function () {
      expect(assetsService.initFilterMap()).toEqual(
        [
          { key: 'js', label: 'JavaScript', filter: true},
          { key: 'css', label: 'CSS', filter: true},
          { key: 'img', label: 'Images', filter: true}
        ]);
    });

    it('getSources should return a table containing Local and External', function () {
      expect(assetsService.getSources()).toEqual([
        { key: 'External', label: 'External'},
        { key: 'Local', label: 'Local'}]);
    });

    it('getExternalSource should return External', function () {
      expect(assetsService.getExternalSource()).toBe('External');
    });

    it('getTypes should return a table with all the types ', function () {
      expect(assetsService.getTypes()).toEqual(
        [
          { key: 'js', label: 'JavaScript'},
          { key: 'css', label: 'CSS'},
          { key: 'img', label: 'Images'}
        ]);
    });
  });

  describe('assetToForm', function () {
    it('should return an object with default value for type and source', function () {
      expect(assetsService.assetToForm()).toEqual( {
        type : 'js',
        source : 'External'
      });
    });

    it('should return an object with external asset value ', function () {
      var asset = {
        name : 'http://asset.css',
        type : 'css'
      };
      expect(assetsService.assetToForm(asset)).toEqual( {
        name : 'http://asset.css',
        type : 'css',
        source : 'External',
        oldname : 'http://asset.css',
        oldtype : 'css'
      });
    });

    it('should return an object with local asset value ', function () {
      var asset = {
        name : 'asset.css',
        type : 'css'
      };
      expect(assetsService.assetToForm(asset)).toEqual( {
        name : 'asset.css',
        type : 'css',
        source : 'Local',
        oldname : 'asset.css',
        oldtype : 'css'
      });
    });
  });

  describe('formToAsset', function () {
    it('should return an asset', function () {
      var formasset = {
        name : 'http://asset.css',
        type : 'css',
        source : 'External'
      };
      expect(assetsService.formToAsset(formasset)).toEqual( {
        name : 'http://asset.css',
        type : 'css'
      });
    });
  });

});
