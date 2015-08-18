describe('utils', function () {
  var assetsService, gettextCatalog, assetsServiceProvider;

  beforeEach(module('bonitasoft.designer.assets', function (_assetsServiceProvider_) {
    assetsServiceProvider = _assetsServiceProvider_;
  }));

  beforeEach(inject(function ($injector) {
    assetsService = $injector.get('assetsService');
    gettextCatalog = $injector.get('gettextCatalog');
  }));


  describe('provider', function () {

    it('should allow registering new data types', function () {
      var newDataType = {
        key: 'foo', value: 'bar', filter: true, template: 'baz'
      };

      assetsServiceProvider.registerType(newDataType);

      expect(assetsServiceProvider.$get(gettextCatalog).getType()['foo']).toEqual(newDataType);
    });

  });

  describe('data', function () {


    it('getSources should return a table containing Local and External', function () {
      expect(assetsService.getSource()).toEqual({
        external: {key: 'external', value: 'External'},
        local: {key: 'local', value: 'Local'}
      });
    });

    it('getTypes should return a table with all the types ', function () {
      expect(assetsService.getType()).toEqual(
        {
          js: {key: 'js', value: 'JavaScript', filter: true, template: 'js/assets/generic-asset-form.html'},
          css: {key: 'css', value: 'CSS', filter: true, template: 'js/assets/generic-asset-form.html'},
          img: {key: 'img', value: 'Image', filter: true, template: 'js/assets/generic-asset-form.html'}
        });
    });
  });

  describe('assetToForm', function () {
    it('should return an object with default value for type and source', function () {
      expect(assetsService.assetToForm()).toEqual({
        type: 'js',
        source: 'external'
      });
    });

    it('should return an object with external asset value ', function () {
      var asset = {
        id: 'UIID',
        name: 'http://asset.css',
        type: 'css'
      };
      expect(assetsService.assetToForm(asset)).toEqual({
        id: 'UIID',
        name: 'http://asset.css',
        type: 'css',
        source: 'external',
        oldname: 'http://asset.css',
        oldtype: 'css'
      });
    });

    it('should return an object with local asset value ', function () {
      var asset = {
        id: 'UIID',
        name: 'asset.css',
        type: 'css'
      };
      expect(assetsService.assetToForm(asset)).toEqual({
        id: 'UIID',
        name: 'asset.css',
        type: 'css',
        source: 'local',
        oldname: 'asset.css',
        oldtype: 'css'
      });
    });
  });

  describe('formToAsset', function () {
    it('should return an asset', function () {
      var formasset = {
        id: 'UIID',
        name: 'http://asset.css',
        type: 'css',
        source: 'external'
      };
      expect(assetsService.formToAsset(formasset)).toEqual({
        id: 'UIID',
        name: 'http://asset.css',
        type: 'css'
      });
    });
  });

});
