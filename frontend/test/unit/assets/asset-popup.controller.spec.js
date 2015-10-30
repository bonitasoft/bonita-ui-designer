describe('AssetPopupCtrl', function() {

  var $rootScope, $scope, asset, $modalInstance, assetsService, alerts, assetsServiceProvider, controller, artifactRepo, $q, injector;

  function createController(mode) {
    return injector.get('$controller')('AssetPopupCtrl', {
      $scope: $rootScope.$new(),
      $modalInstance: $modalInstance,
      asset: asset,
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
    $modalInstance = jasmine.createSpyObj('$modalInstance', ['dismiss', 'close']);
    alerts = jasmine.createSpyObj('alerts', ['addError']);

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
    expect($modalInstance.dismiss).toHaveBeenCalled();
  });

  it('should diplay error and close modal when response contains error after form submit', function() {
    var response = { type: 'error', message: 'an error occured' };

    controller.onComplete(response);

    expect(alerts.addError).toHaveBeenCalledWith(response.message);
    expect($modalInstance.close).toHaveBeenCalled();
  });

  it('should send external data to the caller when user want to save it', function() {
    var data = { name: 'myasset.js' };
    spyOn(artifactRepo, 'createAsset').and.returnValue($q.when(data));

    controller.saveExternalAsset(data);

    expect(artifactRepo.createAsset).toHaveBeenCalled();
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

  it('should expose form templates to the scope', function() {
    expect(controller.templates).toEqual({
      js: 'js/assets/generic-asset-form.html',
      css: 'js/assets/generic-asset-form.html',
      img: 'js/assets/generic-asset-form.html'
    });
  });
});
