describe('pageRepo', function() {
  var $rootScope, pageRepo, $httpBackend;

  var json = {
    id: 'person',
    'rows': []
  };

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories'));
  beforeEach(inject(function(_$rootScope_, $q, _pageRepo_, _$httpBackend_) {
    $rootScope = _$rootScope_;
    pageRepo = _pageRepo_;
    $httpBackend = _$httpBackend_;
  }));

  it('should list the pages', function() {
    var expectedPages = [
      {
        id: 'f1',
        name: 'page1'
      }
    ];
    $httpBackend.expectGET('rest/pages').respond(expectedPages);

    var pages;
    pageRepo.all().then(function(data) {
      pages = data;
    });

    $httpBackend.flush();
    expect(pages).toEqual(expectedPages);
  });

  it('should create a page', function() {
    var page = {
      name: 'foo',
      rows: [[]]
    };

    var createdPage = {
      id: 'generated-id',
      name: 'foo',
      rows: [[]]
    };

    $httpBackend.expectPOST('rest/pages').respond(201, createdPage);

    var result;
    pageRepo.create(page).then(function(data) {
      result = data;
    });

    $httpBackend.flush();
    expect(result).toEqual(createdPage);
  });

  it('should duplicate a page', function() {
    var page = {
      name: 'foo',
      rows: [[]]
    };

    var createdPage = {
      id: 'generated-id',
      name: 'foo',
      rows: [[]]
    };

    $httpBackend.expectPOST('rest/pages?duplicata=src-page-id').respond(201, createdPage);

    var result;
    pageRepo.create(page, 'src-page-id').then(function(data) {
      result = data;
    });

    $httpBackend.flush();
    expect(result).toEqual(createdPage);
  });

  it('should save a page', function() {
    // given a page
    $httpBackend.expectPUT('rest/pages/person').respond(204);

    // when we save it
    pageRepo.save(json);

    // then we should have called the backend
    $httpBackend.flush();
  });

  it('should delete a page', function() {
    // given a page
    $httpBackend.expectDELETE('rest/pages/person').respond(200);

    // when we delete it
    pageRepo.delete('person', json);

    // then we should have called the backend
    $httpBackend.flush();
  });

  it('should load a page', function() {
    // given a page
    $httpBackend.expectGET('rest/pages/person').respond(200, json);

    // when we load it
    var pageData;
    pageRepo.load('person')
      .success(function(data) {
        pageData = data;
      });

    // then we should have called the backend
    $httpBackend.flush();
  });

  it('should compute page export url', function() {
    var page = { id: 'aPageId' };

    var url = pageRepo.exportUrl(page);

    expect(url).toBe('export/page/aPageId');
  });

  it('should rename a page', function() {
    pageRepo.rename('person', 'hello');
    $httpBackend.expectPUT('rest/pages/person/name').respond(200, 'hello');
    $httpBackend.flush();
  });

  it('should list the page assets', function() {
    var expectedAssets = [
      { name: 'asset1' }
    ];
    $httpBackend.expectGET('rest/pages/1/assets').respond(expectedAssets);

    var assets;
    pageRepo.loadAssets({ id: 1 }).then(function(data) {
      assets = data;
    });

    $httpBackend.flush();
    expect(assets).toEqual(expectedAssets);
  });

  it('should delete a local asset', function() {
    var asset = {
      id: 'UIID',
      name: 'myfile.js',
      type: 'js'
    };
    $httpBackend.expectDELETE('rest/pages/page1/assets/UIID').respond(200);

    pageRepo.deleteAsset('page1', asset);
    $httpBackend.flush();
  });

  it('should delete an external asset', function() {
    var asset = {
      id: 'UIID',
      name: 'http://mycdn.com/myfile.js',
      type: 'js'
    };
    $httpBackend.expectDELETE('rest/pages/page1/assets/UIID').respond(200);

    pageRepo.deleteAsset('page1', asset);
    $httpBackend.flush();
  });

  it('should deactivate an asset', function() {
    var asset = {
      id: 'UIID',
      name: 'http://mycdn.com/myfile.js',
      type: 'js',
      active: false
    };
    $httpBackend.expectPUT('rest/pages/page1/assets/UIID?active=false').respond(200);

    pageRepo.desactivateAsset('page1', asset);
    $httpBackend.flush();
    $httpBackend.expectPUT('rest/pages/page1/assets/UIID?active=true').respond(200);
    asset.active = true;
    pageRepo.desactivateAsset('page1', asset);
    $httpBackend.flush();
  });

  it('should save an asset', function() {
    var asset = {
      name: 'http://mycdn.com/myfile.js',
      type: 'js'
    };
    $httpBackend.expectPOST('rest/pages/my-page/assets').respond(200);
    pageRepo.createAsset('my-page', asset);
    $httpBackend.flush();
  });

  it('should mark a page as favorite', function() {
    $httpBackend.expectPUT('rest/pages/page-id/favorite', true).respond('');

    pageRepo.markAsFavorite('page-id');

    $httpBackend.flush();
  });

  it('should unmark a page as favorite', function() {
    $httpBackend.expectPUT('rest/pages/page-id/favorite', false).respond('');

    pageRepo.unmarkAsFavorite('page-id');

    $httpBackend.flush();
  });
});
