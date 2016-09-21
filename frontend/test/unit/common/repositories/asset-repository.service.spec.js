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

  it('should update a local asset content', () => {
    var asset = {
      id: 'UIID',
      name: 'myfile.js',
      type: 'js'
    };
    $httpBackend.expectPOST(
      'a/base/url/aPageId/assets/js',
      () => true, // seems that we cannot test expected data. Phantomjs seems to not support FormData
      (headers) => !headers['Content-Type']    // don't want any content-type, should be set by browser
    ).respond(200);

    assetRepo.updateLocalAssetContent('aPageId', asset, 'new file content');

    $httpBackend.flush();
  });

  it('should load a local asset content', () => {
    var asset = { id: 'asset-id', name: 'myfile.js', type: 'js' };
    $httpBackend.whenGET('a/base/url/page-id/assets/js/myfile.js?format=text')
      .respond('{ "json": "file" }');

    assetRepo.loadLocalAssetContent('page-id', asset)
      .then((response) =>  expect(response.data).toEqual('{ "json": "file" }'));

    $httpBackend.flush();
  });
});
