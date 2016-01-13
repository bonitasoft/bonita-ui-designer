describe('AssetPreviewPopupCtrl', function() {

  var $uibModalInstance, $controller, $rootScope, assetsService;

  beforeEach(angular.mock.module('bonitasoft.designer.assets'));

  beforeEach(inject(function(_$controller_, _$rootScope_, $injector) {
    $controller = _$controller_;
    $rootScope = _$rootScope_;
    $uibModalInstance = jasmine.createSpyObj('$uibModalInstance', ['dismiss', 'close']);
    assetsService = $injector.get('assetsService');
  }));

  it('should close modal', function() {
    var scope = $rootScope.$new();
    $controller('AssetPreviewPopupCtrl', {
      $scope: scope,
      $uibModalInstance: $uibModalInstance,
      asset: {
        name: 'myasset.js',
        type: 'js'
      },
      component: { id: 1234 },
      mode: 'page'
    });

    scope.cancel();

    expect($uibModalInstance.dismiss).toHaveBeenCalled();
  });

  it('should get url for widget mode', function() {
    var scope = $rootScope.$new();
    $controller('AssetPreviewPopupCtrl', {
      $uibModalInstance: $uibModalInstance,
      $scope: scope,
      asset: {
        name: 'myasset.js',
        type: 'js'
      },
      component: { id: 1234 },
      mode: 'widget',
      assetsService: assetsService
    });

    expect(scope.url).toBe('rest/widgets/1234/assets/js/myasset.js?format=text');
  });

  it('should get url for widget asset in page mode', function() {
    var scope = $rootScope.$new();
    $controller('AssetPreviewPopupCtrl', {
      $scope: scope,
      $uibModalInstance: $uibModalInstance,
      asset: {
        scope: 'widget',
        componentId: 4321,
        name: 'myasset.js',
        type: 'js'
      },
      component: { id: 56 },
      mode: 'page',
      assetsService: assetsService
    });

    expect(scope.url).toBe('rest/widgets/4321/assets/js/myasset.js?format=text');
  });

  it('should get page asset url', function() {
    var scope = $rootScope.$new();
    $controller('AssetPreviewPopupCtrl', {
      $scope: scope,
      $uibModalInstance: $uibModalInstance,
      asset: {
        name: 'myasset.js',
        type: 'js'
      },
      component: { id: 1234 },
      mode: 'page',
      assetsService: assetsService
    });

    expect(scope.url).toBe('rest/pages/1234/assets/js/myasset.js?format=text');
  });

});
