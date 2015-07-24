describe('utils', function () {
  var assetsService;

  beforeEach(module('bonitasoft.designer.assets'));

  beforeEach(inject(function ($injector) {
    assetsService = $injector.get('assetsService');
  }));

  describe('data', function () {


    it('getSources should return a table containing Local and External', function () {
      expect(assetsService.getSource()).toEqual({
        external : {key : 'external', value: 'External'},
        local : {key : 'local', value: 'Local'}
      });
    });

    it('getTypes should return a table with all the types ', function () {
      expect(assetsService.getType()).toEqual(
        {
          js : {key : 'js', value: 'JavaScript', filter:true },
          css : {key : 'css', value: 'CSS', filter:true},
          img : {key : 'img', value: 'Image', filter:true}
        });
    });
  });

  describe('assetToForm', function () {
    it('should return an object with default value for type and source', function () {
      expect(assetsService.assetToForm()).toEqual( {
        type : 'js',
        source : 'external'
      });
    });

    it('should return an object with external asset value ', function () {
      var asset = {
        id: 'UIID',
        name : 'http://asset.css',
        type : 'css'
      };
      expect(assetsService.assetToForm(asset)).toEqual( {
        id: 'UIID',
        name : 'http://asset.css',
        type : 'css',
        source : 'external',
        oldname : 'http://asset.css',
        oldtype : 'css'
      });
    });

    it('should return an object with local asset value ', function () {
      var asset = {
        id: 'UIID',
        name : 'asset.css',
        type : 'css'
      };
      expect(assetsService.assetToForm(asset)).toEqual( {
        id: 'UIID',
        name : 'asset.css',
        type : 'css',
        source : 'local',
        oldname : 'asset.css',
        oldtype : 'css'
      });
    });
  });

  describe('formToAsset', function () {
    it('should return an asset', function () {
      var formasset = {
        id: 'UIID',
        name : 'http://asset.css',
        type : 'css',
        source : 'external'
      };
      expect(assetsService.formToAsset(formasset)).toEqual( {
        id: 'UIID',
        name : 'http://asset.css',
        type : 'css'
      });
    });
  });

});
