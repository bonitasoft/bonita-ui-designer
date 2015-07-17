describe('AssetPreviewPopupCtrl', function() {

  var $scope, asset, $modalInstance;

  beforeEach(module('bonitasoft.ui.assets'));

  beforeEach(inject(function($injector) {
    $scope = $injector.get('$rootScope').$new();
    $modalInstance = jasmine.createSpyObj('$modalInstance', ['dismiss', 'close']);
    asset = {
      name : 'myasset.js',
      type : 'js'
    };
    $injector.get('$controller')('AssetPreviewPopupCtrl', {
        $scope: $scope,
        $modalInstance: $modalInstance,
        asset: asset,
        url : 'http://designer/preview/asset'
      });
  }));

  it('should close modal', function() {
    $scope.cancel();
    expect($modalInstance.dismiss).toHaveBeenCalled();
  });

  it('should init data in scope', function() {
    $scope.$digest();
    expect($scope.asset).toEqual(asset);
    expect($scope.url).toBe('http://designer/preview/asset');
  });

});
