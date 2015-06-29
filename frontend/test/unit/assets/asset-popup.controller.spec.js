describe('AssetPopupCtrl', function() {

  var $scope, asset, $modalInstance, assetsService, alerts;

  beforeEach(module('pb.assets'));

  beforeEach(inject(function($injector) {
    $scope = $injector.get('$rootScope').$new();
    assetsService = $injector.get('assetsService');

    $modalInstance = jasmine.createSpyObj('$modalInstance', ['dismiss', 'close']);
    alerts = jasmine.createSpyObj('alerts', ['addError']);

    asset = {
      name : 'myasset.js',
      type : 'js'
    };


    $injector.get('$controller')('AssetPopupCtrl', {
      $scope: $scope,
      $modalInstance: $modalInstance,
      asset: asset,
      assetsService : assetsService,
      alerts:alerts,
      mode : 'page',
      artifact:{id:12}
    });
  }));


  it('should close modal', function() {
    $scope.cancel();
    expect($modalInstance.dismiss).toHaveBeenCalled();
  });

  it('should diplay error and dismiss modal onError after form submit', function() {
    var error = {error : 'error', message: 'an error occured'};

    $scope.onError(error);

    expect(alerts.addError).toHaveBeenCalledWith(error.message);
    expect($modalInstance.dismiss).toHaveBeenCalled();
  });

  it('should diplay error and close modal when response contains error after form submit', function() {
    var response = {type : 'error', message: 'an error occured'};
    $scope.onSuccess(response);
    expect(alerts.addError).toHaveBeenCalledWith(response.message);
    expect($modalInstance.close).toHaveBeenCalled();
  });

  it('should send external data to the caller when user want to save it', function() {
    var data = {name : 'myasset.js'};
    $scope.saveExternalAsset(data);
    expect($modalInstance.close).toHaveBeenCalledWith(data);
  });
});
