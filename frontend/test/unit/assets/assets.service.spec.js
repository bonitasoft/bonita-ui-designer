describe('utils', function() {
  var assetsService, gettextCatalog, assetsServiceProvider;

  beforeEach(angular.mock.module('bonitasoft.designer.assets', function(_assetsServiceProvider_) {
    assetsServiceProvider = _assetsServiceProvider_;
  }));

  beforeEach(angular.mock.inject(function($injector) {
    assetsService = $injector.get('assetsService');
    gettextCatalog = $injector.get('gettextCatalog');
  }));

  describe('provider', function() {

    it('should allow registering new data types', function() {
      var newDataType = [{
        key: 'foo', value: 'bar', filter: true, template: 'baz'
      }];

      assetsServiceProvider.registerType(newDataType);

      expect(assetsServiceProvider.$get(gettextCatalog).getTypes()).toContain(newDataType);
    });

  });

  describe('data', function() {

    it('getSources should return a table containing Local and External', function() {
      expect(assetsService.getSources()).toEqual({
        external: { key: true, value: 'External' },
        local: { key: false, value: 'Local' }
      });
    });

    it('getTypes should return a table with all the types ', function() {
      expect(assetsService.getTypes()).toEqual(
        [
          { key: 'css', value: 'CSS', filter: true, widget: true, template: 'js/assets/generic-asset-form.html', aceMode: 'css', orderable: true },
          { key: 'js', value: 'JavaScript', filter: true, widget: true, template: 'js/assets/generic-asset-form.html', aceMode: 'javascript', orderable: true },
          { key: 'img', value: 'Image', filter: true, widget: true, template: 'js/assets/generic-asset-form.html', orderable: false }
        ]);
    });
  });

  describe('assetToForm', function() {
    it('should return an object with default value for type and source', function() {
      expect(assetsService.assetToForm()).toEqual({
        type: 'css',
        external: true
      });
    });

    it('should return an object with external asset value ', function() {
      var asset = {
        id: 'UIID',
        name: 'http://asset.css',
        type: 'css',
        order: 2,
        external: true,
        scope: 'page'
      };
      expect(assetsService.assetToForm(asset)).toEqual({
        id: 'UIID',
        name: 'http://asset.css',
        type: 'css',
        external: true,
        oldname: 'http://asset.css',
        oldtype: 'css',
        order: 2,
        scope: 'page'
      });
    });

    it('should return an object with local asset value ', function() {
      var asset = {
        id: 'UIID',
        name: 'asset.css',
        type: 'css',
        order: 2,
        scope: 'page'
      };
      expect(assetsService.assetToForm(asset)).toEqual({
        id: 'UIID',
        name: 'asset.css',
        type: 'css',
        external: undefined,
        oldname: 'asset.css',
        oldtype: 'css',
        order: 2,
        scope: 'page'
      });
    });
  });

  describe('formToAsset', function() {
    it('should return an asset', function() {
      var formasset = {
        id: 'UIID',
        name: 'http://asset.css',
        type: 'css',
        external: true,
        scope: 'page',
        order: 2
      };
      expect(assetsService.formToAsset(formasset, 'page')).toEqual({
        id: 'UIID',
        name: 'http://asset.css',
        type: 'css',
        scope: 'page',
        order: 2,
        external: true
      });
    });
  });

  it('should add widget asset before page assets if not already added', function() {
    var page = {
      assets: [
          { name: 'aJsAsset', type: 'js', componentId: 'anotherWidget', scope: 'widget' },
          { name: 'anAsset', type: 'css', componentId: 'theWidget', scope: 'widget' }
        ]
    };

    var widget = {
      id: 'theWidget',
      $$widget: {
        assets: [
          { name: 'aJsAsset', type: 'js' },
          { name: 'aCssAsset', type: 'css' },
          { name: 'anotherAsset', type: 'css' }
        ]
      }
    };

    assetsService.addWidgetAssetsToPage(widget, page);

    expect(page.assets).toEqual([
      { name: 'aJsAsset', type: 'js', componentId: 'theWidget', scope: 'widget' },
      { name: 'aCssAsset', type: 'css', componentId: 'theWidget', scope: 'widget' },
      { name: 'anotherAsset', type: 'css', componentId: 'theWidget', scope: 'widget' },
      { name: 'aJsAsset', type: 'js', componentId: 'anotherWidget', scope: 'widget' },
      { name: 'anAsset', type: 'css', componentId: 'theWidget', scope: 'widget' }
    ]);
  });

  it('should not add widget asset to page if already added', function() {
    var page = {
      assets: [
        { name: 'widgetAsset',  type: 'js', componentId: 'theWidget', scope: 'widget' },
        { name: 'otherWidgetAsset', type: 'js', componentId: 'theWidget', scope: 'widget' },
        { name: 'anAsset', type: 'js', componentId: '1234' }]
    };
    var widget = {
      id: 'theWidget',
      $$widget: {
        assets: [
          { name: 'widgetAsset',  type: 'js' },
          { name: 'otherWidgetAsset', type: 'js' }
        ]
      }
    };

    assetsService.addWidgetAssetsToPage(widget, page);

    expect(page.assets).toEqual([
      { name: 'widgetAsset',  type: 'js', componentId: 'theWidget', scope: 'widget' },
      { name: 'otherWidgetAsset', type: 'js', componentId: 'theWidget', scope: 'widget' },
      { name: 'anAsset', type: 'js', componentId: '1234' }
    ]);
  });

  it('should populate asset scope and componentId if not set while adding asset to page', function() {
    var page = {
      assets: []
    };
    var widget = {
      id: 'widgetId',
      $$widget: {
        assets: [
          { id: 'widgetAsset' },
          { id: 'otherWidgetAsset' }]
      }
    };

    assetsService.addWidgetAssetsToPage(widget, page);

    expect(page.assets).toEqual([
      { id: 'widgetAsset', componentId: 'widgetId', scope: 'widget' },
      { id: 'otherWidgetAsset', componentId: 'widgetId', scope: 'widget' }
    ]);
  });

  it('should remove widget assets from page', function() {
    var page = {
      assets: [
        { id: 'widgetAsset', componentId: 'widgetId', scope: 'widget' },
        { id: 'otherWidgetAsset', componentId: 'widgetId', scope: 'widget' },
        { id: 'anAsset', componentId: '1234' }]
    };
    var widget = {
      id: 'widgetId'
    };

    assetsService.removeAssetsFromPage(widget, page);

    expect(page.assets).toEqual([{ id: 'anAsset', componentId: '1234' }]);
  });

  it('should get a specific type', function() {
    expect(assetsService.getType('css')).toBe(assetsService.getTypes()[0]);
    expect(assetsService.getType('js')).toBe(assetsService.getTypes()[1]);
    expect(assetsService.getType('img')).toBe(assetsService.getTypes()[2]);
  });

});
