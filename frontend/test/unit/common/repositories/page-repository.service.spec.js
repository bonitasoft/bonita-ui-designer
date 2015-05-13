describe('pageRepo', function() {
  var $rootScope, pageRepo, $httpBackend;

  var json = {
    "rows": []
  };

  beforeEach(module('pb.common.repositories'));
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

  it('should save a page', function() {
    // given a page
    $httpBackend.expectPUT('rest/pages/person').respond(204);

    // when we save it
    pageRepo.save('person', json);

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

  it('should delete a page data', function() {
    $httpBackend.expectDELETE('rest/pages/person/data/aData').respond(200, []);
    json.id = 'person';

    pageRepo.deleteData(json, "aData");
    $httpBackend.flush();
  });

  it('should save a page data', function() {
    var data = {$$name: "aData", value: "aValue"};
    $httpBackend.expectPUT('rest/pages/person/data/aData').respond(200, [data]);
    json.id = 'person';

    pageRepo.saveData(json, data);
    $httpBackend.flush();
  });

  it('should compute page export url', function() {
    var page = {id: 'aPageId'};

    var url = pageRepo.exportUrl(page);

    expect(url).toBe('export/page/aPageId');
  });

  it('should rename a page', function() {
    pageRepo.rename('person', 'hello');
    $httpBackend.expectPUT('rest/pages/person/name').respond(200, 'hello');
    $httpBackend.flush();
  });
});
