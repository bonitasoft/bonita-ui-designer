describe('AssetRepository', () => {

  var assetRepo, $httpBackend;

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories'));

  beforeEach(inject(function (AssetRepository, _$httpBackend_) {
    assetRepo = new AssetRepository('a/base/url');
    $httpBackend = _$httpBackend_;
  }));

  it('should create an external asset', () => {
    var asset = {
      name: 'http://mycdn.com/myfile.js',
      type: 'js'
    };
    $httpBackend.expectPOST('a/base/url/mypage/assets').respond(200);

    assetRepo.createAsset('mypage', asset);
    $httpBackend.flush();
  });

  it('should delete a local asset', function () {
    var asset = {
      id: 'UIID',
      name: 'myfile.js',
      type: 'js'
    };
    $httpBackend.expectDELETE('a/base/url/page1/assets/UIID').respond(200);

    assetRepo.deleteAsset('page1', asset);
    $httpBackend.flush();
  });

  it('should delete an external asset', function () {
    var asset = {
      id: 'UIID',
      name: 'http://mycdn.com/myfile.js',
      type: 'js'
    };
    $httpBackend.expectDELETE('a/base/url/page1/assets/UIID').respond(200);

    assetRepo.deleteAsset('page1', asset);
    $httpBackend.flush();
  });

});
