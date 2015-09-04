describe('AssetPopupCtrl', function () {

  var $rootScope, $scope, asset, $modalInstance, assetsService, alerts, assetsServiceProvider, createController;

  beforeEach(module('bonitasoft.designer.assets', function (_assetsServiceProvider_) {
    assetsServiceProvider = _assetsServiceProvider_;
  }));

  beforeEach(inject(function ($injector) {
    $rootScope = $injector.get('$rootScope');
    $scope = $rootScope.$new();
    assetsService = $injector.get('assetsService');

    $modalInstance = jasmine.createSpyObj('$modalInstance', ['dismiss', 'close']);
    alerts = jasmine.createSpyObj('alerts', ['addError']);

    asset = {
      name: 'myasset.js',
      type: 'js'
    };

    createController = function (scope, mode) {
      $injector.get('$controller')('AssetPopupCtrl', {
        $scope: scope,
        $modalInstance: $modalInstance,
        asset: asset,
        assetsService: assetsService,
        alerts: alerts,
        mode: mode,
        artifact: {id: 12}
      });
    };

    createController($scope, 'page');
  }));


  it('should close modal', function () {
    $scope.cancel();
    expect($modalInstance.dismiss).toHaveBeenCalled();
  });

  it('should diplay error and dismiss modal onError after form submit', function () {
    var error = {error: 'error', message: 'an error occured'};

    $scope.onError(error);

    expect(alerts.addError).toHaveBeenCalledWith(error.message);
    expect($modalInstance.dismiss).toHaveBeenCalled();
  });

  it('should diplay error and close modal when response contains error after form submit', function () {
    var response = {type: 'error', message: 'an error occured'};
    $scope.onSuccess(response);
    expect(alerts.addError).toHaveBeenCalledWith(response.message);
    expect($modalInstance.close).toHaveBeenCalled();
  });

  it('should send external data to the caller when user want to save it', function () {
    var data = {name: 'myasset.js'};
    $scope.saveExternalAsset(data);
    expect($modalInstance.close).toHaveBeenCalledWith(data);
  });

  it('should expose asset types to the scope', function () {
    expect($scope.assetTypes.map(function (type) {
      return type.key;
    })).toEqual(['js', 'css', 'img']);
  });

  it('should expose only widget asset types to the scope when in widget mode', function () {
    assetsServiceProvider.registerType({
      key: 'foo',
      widget: false
    });
    var $scope = $rootScope.$new();
    createController($scope, 'widget');

    expect($scope.assetTypes.map(function (type) {
      return type.key;
    })).toEqual(['js', 'css', 'img']);
  });

  it('should expose form templates to the scope', function () {
    expect($scope.templates).toEqual({
      js: 'js/assets/generic-asset-form.html',
      css: 'js/assets/generic-asset-form.html',
      img: 'js/assets/generic-asset-form.html'
    });
  });
});
