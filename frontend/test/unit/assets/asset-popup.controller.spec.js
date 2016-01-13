describe('AssetPopupCtrl', function() {

  var $rootScope, $scope, asset, $uibModalInstance, assetsService, alerts, assetsServiceProvider, controller, artifactRepo, $q, injector, assets;

  function createController(mode) {
    return injector.get('$controller')('AssetPopupCtrl', {
      $scope: $rootScope.$new(),
      $uibModalInstance: $uibModalInstance,
      asset: asset,
      assets: assets,
      assetsService: assetsService,
      alerts: alerts,
      mode: mode,
      artifact: { id: 12 },
      artifactRepo: artifactRepo
    });
  }

  beforeEach(angular.mock.module('bonitasoft.designer.assets', function(_assetsServiceProvider_) {
    assetsServiceProvider = _assetsServiceProvider_;
  }));

  beforeEach(inject(function($injector) {
    $q = $injector.get('$q');
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    assetsService = $injector.get('assetsService');
    injector = $injector;
    $uibModalInstance = jasmine.createSpyObj('$uibModalInstance', ['dismiss', 'close']);
    alerts = jasmine.createSpyObj('alerts', ['addError']);

    assets = [];
    asset = {
      name: 'myasset.js',
      type: 'js'
    };

    artifactRepo = {
      loadAssets: function() {
        return $q.when([
          { id: '123', name: 'myAsset', scope: 'PAGE', active: true },
          { id: '456', name: 'myPrivateDeactivatedAsset', scope: 'PAGE', active: false },
          { id: '789', name: 'publicAsset', scope: 'WIDGET', active: true },
          { id: '321', name: 'publicDeactivatedAsset', scope: 'WIDGET', active: false }
        ]);
      },
      deleteAsset: function() {
      },
      createAsset: function() {
        return $q.when({});
      }
    };

    controller = createController('page');

  }));

  it('should close modal', function() {
    controller.cancel();
    expect($uibModalInstance.dismiss).toHaveBeenCalled();
  });

  it('should diplay error and close modal when response contains error after form submit', function() {
    var response = { type: 'error', message: 'an error occured' };

    controller.onComplete(response);

    expect(alerts.addError).toHaveBeenCalledWith(response.message);
    expect($uibModalInstance.close).toHaveBeenCalled();
  });

  it('should display a specific error while uploading a malformed json file for json asset', function() {
    var response = { type: 'MalformedJsonException', message: 'an error occured', infos: { location: {column: 65, line: 12}} };

    controller.onComplete(response);

    expect(alerts.addError).toHaveBeenCalledWith({
        contentUrl: 'js/assets/malformed-json-error-message.html',
        context: response
      }, 12000);
    expect($uibModalInstance.close).toHaveBeenCalled();
  });

  it('should send external data to the caller when user want to save it', function() {
    var data = { name: 'myasset.js', external: true};
    spyOn(artifactRepo, 'createAsset').and.returnValue($q.when(data));
    let event = jasmine.createSpyObj('event', ['preventDefault']);

    controller.saveExternalAsset(data, event);

    expect(artifactRepo.createAsset).toHaveBeenCalled();
    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('should not save a non external asset', function() {
    var data = { name: 'myasset.js', source: 'local'};
    spyOn(artifactRepo, 'createAsset').and.returnValue($q.when(data));
    let event = jasmine.createSpyObj('event', ['preventDefault']);

    controller.saveExternalAsset(data, event);

    expect(artifactRepo.createAsset).not.toHaveBeenCalled();
    expect(event.preventDefault).not.toHaveBeenCalled();
  });

  it('should expose asset types to the scope', function() {
    expect(controller.assetTypes.map(function(type) {
      return type.key;
    })).toEqual(['js', 'css', 'img']);
  });

  it('should expose only widget asset types to the scope when in widget mode', function() {
    assetsServiceProvider.registerType({
      key: 'foo',
      widget: false
    });
    controller = createController('widget');

    expect(controller.assetTypes.map(function(type) {
      return type.key;
    })).toEqual(['js', 'css', 'img']);
  });
  it('should validate url', function() {
    expect(controller.urlPattern.test('http://server:123/path')).toBe(true);
    expect(controller.urlPattern.test('https://server:123/path')).toBe(true);
    expect(controller.urlPattern.test('//server:123/path')).toBe(true);

    expect(controller.urlPattern.test('/foo')).toBe(true);
    expect(controller.urlPattern.test('../../foo')).toBe(true);
    expect(controller.urlPattern.test('foo.txt')).toBe(true);

    expect(controller.urlPattern.test('../../foo test')).toBe(false);
    expect(controller.urlPattern.test('http://example.com:9999/~~``')).toBe(false);
  });

  it('should expose form templates to the scope', function() {
    expect(controller.templates).toEqual({
      js: 'js/assets/generic-asset-form.html',
      css: 'js/assets/generic-asset-form.html',
      img: 'js/assets/generic-asset-form.html'
    });
  });

  it('should tell that an asset on current scope already exists', function() {
    assets = [
      {type: 'js', name: 'myFile.js'},
      {type: 'css', name: 'asset.css', scope: 'widget'}
    ];
    controller = createController('page');

    expect(controller.isExisting({type: 'js', name: 'myFile.js'})).toBeTruthy();
    expect(controller.isExisting({type: 'js', name: 'myOtherFile.js'})).toBeFalsy();
    expect(controller.isExisting({type: 'css', name: 'myFile.js'})).toBeFalsy();
    expect(controller.isExisting({type: 'css', name: 'asset.css'})).toBeFalsy();
  });

  it('should reset new asset name when new asset source change', function() {
    asset = {name: 'anAsset', source: 'local'};
    controller = createController('page');
    $scope.$apply();
    expect(controller.newAsset.name).toBe('anAsset');

    controller.newAsset.external = true;
    $scope.$apply();
    expect(controller.newAsset.name).toBeUndefined();
  });

  it('should get a warning message for image asset', function() {
    expect(controller.getWarningMessage({name: 'image.png', type: 'img'})).toBe('An Image asset named <em>image.png</em> is already added to assets.');
  });

  it('should get a warning message for other asset', function() {
    expect(controller.getWarningMessage({name: 'file.css', type: 'css'})).toBe('A CSS asset named <em>file.css</em> is already added to assets.');
    expect(controller.getWarningMessage({name: 'file.js', type: 'js'})).toBe('A JavaScript asset named <em>file.js</em> is already added to assets.');
  });
});
