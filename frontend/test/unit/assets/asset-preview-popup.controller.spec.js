describe('AssetPreviewPopupCtrl', function() {

  var $modalInstance, $controller, $rootScope;

  beforeEach(angular.mock.module('bonitasoft.designer.assets'));

  beforeEach(inject(function(_$controller_, _$rootScope_) {
    $controller = _$controller_;
    $rootScope = _$rootScope_;

    $modalInstance = jasmine.createSpyObj('$modalInstance', ['dismiss', 'close']);
  }));

  it('should close modal', function() {
    var scope = $rootScope.$new();
    $controller('AssetPreviewPopupCtrl', {
      $scope: scope,
      $modalInstance: $modalInstance,
      asset: {
        name: 'myasset.js',
        type: 'js'
      },
      component: { id: 1234 },
      mode: 'page'
    });

    scope.cancel();

    expect($modalInstance.dismiss).toHaveBeenCalled();
  });

  it('should get url for widget mode', function() {
    var scope = $rootScope.$new();
    $controller('AssetPreviewPopupCtrl', {
      $modalInstance: $modalInstance,
      $scope: scope,
      asset: {
        name: 'myasset.js',
        type: 'js'
      },
      component: { id: 1234 },
      mode: 'widget'
    });

    expect(scope.url).toBe('preview/widget/1234/assets/js/myasset.js?format=text');
  });

  it('should get url for widget asset in page mode', function() {
    var scope = $rootScope.$new();
    $controller('AssetPreviewPopupCtrl', {
      $scope: scope,
      $modalInstance: $modalInstance,
      asset: {
        scope: 'widget',
        componentId: 4321,
        name: 'myasset.js',
        type: 'js'
      },
      component: { id: 56 },
      mode: 'page'
    });

    expect(scope.url).toBe('preview/widget/4321/assets/js/myasset.js?format=text');
  });

  it('should get page asset url', function() {
    var scope = $rootScope.$new();
    $controller('AssetPreviewPopupCtrl', {
      $scope: scope,
      $modalInstance: $modalInstance,
      asset: {
        name: 'myasset.js',
        type: 'js'
      },
      component: { id: 1234 },
      mode: 'page'
    });

    expect(scope.url).toBe('preview/page/1234/assets/js/myasset.js?format=text');
  });

});
