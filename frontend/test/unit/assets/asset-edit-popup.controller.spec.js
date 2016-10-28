describe('AssetEditPopupCtrl', () => {

  var assetsService, assetRepo, component, asset, $uibModalInstance, $q, $controller, $scope;

  beforeEach(angular.mock.module('bonitasoft.designer.assets', 'mock.modal'));

  beforeEach(inject(function(_$controller_, _$q_, _assetsService_, $rootScope, _$uibModalInstance_, AssetRepository) {
    $q = _$q_;
    $controller = _$controller_;
    assetRepo = new AssetRepository('a/base/url');
    assetsService = _assetsService_;
    $scope = $rootScope;
    $uibModalInstance = _$uibModalInstance_.create();

    asset = { type: 'js' };
    component = { id: 'component-id' };
  }));

  function createController(initialAssetContent) {
    spyOn(assetRepo, 'loadLocalAssetContent').and.returnValue($q.when({ data: initialAssetContent }));
    let ctrl = $controller('AssetEditPopupCtrl', {
      $scope: $scope,
      asset: asset,
      component: component,
      aceMode: 'css',
      $uibModalInstance: $uibModalInstance,
      assetRepo: assetRepo
    });
    $scope.$apply();
    return ctrl;
  }

  it('should load local asset content', function() {

    let ctrl = createController('initial asset content');

    expect(ctrl.content).toBe('initial asset content');
  });

  it('should save local asset content', function() {
    spyOn(assetRepo, 'updateLocalAssetContent').and.returnValue($q.when());
    spyOn($scope, '$broadcast').and.callThrough();
    let ctrl = createController();

    ctrl.content = 'new content';
    ctrl.save();
    $scope.$apply();

    expect(assetRepo.updateLocalAssetContent).toHaveBeenCalledWith(component.id, asset, 'new content');
    expect($uibModalInstance.close).not.toHaveBeenCalled();
    expect($scope.$broadcast).toHaveBeenCalledWith('saved');
  });

  it('should save local asset content and close pop up', function() {
    spyOn(assetRepo, 'updateLocalAssetContent').and.returnValue($q.when());
    let ctrl = createController();

    ctrl.content = 'new content';
    ctrl.saveAndClose();
    $scope.$apply();

    expect(assetRepo.updateLocalAssetContent).toHaveBeenCalledWith(component.id, asset, 'new content');
    expect($uibModalInstance.close).toHaveBeenCalled();
  });

});
