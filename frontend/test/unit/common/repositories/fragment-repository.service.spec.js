describe('fragmentRepo', function() {
  var fragmentRepo, $httpBackend;

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories', 'bonitasoft.designer.editor.whiteboard'));
  beforeEach(inject(function(_$httpBackend_, _fragmentRepo_) {
    fragmentRepo = _fragmentRepo_;
    $httpBackend = _$httpBackend_;
  }));

  it('should list the fragments', function() {
    var expectedFragment = [
      {
        id: 'f1',
        name: 'fragment1',
        icon: true
      }
    ];
    $httpBackend.expectGET('rest/fragments').respond(expectedFragment);

    var frags;
    fragmentRepo.all().then(function(fragments) {
      frags = fragments;
    });

    $httpBackend.flush();
    expect(frags).toEqual(expectedFragment);
  });

  it('should list the fragments not using a given element', function() {
    var expectedFragment = [
      {
        id: 'f1',
        name: 'fragment1'
      }
    ];
    $httpBackend.expectGET('rest/fragments?notUsedBy=1234').respond(expectedFragment);

    var frags;
    fragmentRepo.allNotUsingElement(1234).then(function(response) {
      frags = response.data;
    });

    $httpBackend.flush();
    expect(frags).toEqual(expectedFragment);
  });

  it('should list the fragments in light view', function() {
    var expectedFragment = [
      {
        id: 'f1',
        name: 'fragment1'
      }
    ];
    $httpBackend.expectGET('rest/fragments?view=light').respond(expectedFragment);

    var frags;
    fragmentRepo.allLight().then(function(data) {
      frags = data;
    });

    $httpBackend.flush();
    expect(frags).toEqual(expectedFragment);
  });

  it('should create a fragment', function() {
    var fragment = {
      name: 'foo',
      container: {
        rows: [[]]
      }
    };

    var createdFragment = {
      id: 'generated-id',
      name: 'foo',
      container: {
        rows: [[]]
      }
    };

    $httpBackend.expectPOST('rest/fragments').respond(201, createdFragment);

    var result;
    fragmentRepo.create(fragment).then(function(data) {
      result = data;
    });

    $httpBackend.flush();
    expect(result).toEqual(createdFragment);
  });

  it('should save a page as fragment', function() {
    var expectedFragment = {
      id: 'person',
      name: 'Person',
      rows: [[]],
      data: {
        aData: { type: 'constant', value: 'aValue' }
      }
    };

    $httpBackend.expectPUT('rest/fragments/person', expectedFragment).respond(200);

    // when saving a fragment
    fragmentRepo.save(expectedFragment);

    // then we should have call the backend
    $httpBackend.flush();
  });

  it('should delete a fragment', function() {
    // given a page
    $httpBackend.expectDELETE('rest/fragments/person').respond(200);

    // when we delete it
    fragmentRepo.delete('person');

    // then we should have called the backend
    $httpBackend.flush();
  });

  it('should load a fragment', function() {
    $httpBackend.expectGET('rest/fragments/person').respond(200);

    // when loading a fragment
    fragmentRepo.load('person');

    // then we should have call the backend
    $httpBackend.flush();
  });

  it('should compute page export url', function() {
    var fragment = { id: 'fragmentId' };

    var url = fragmentRepo.exportUrl(fragment);

    expect(url).toBe('export/fragment/fragmentId');
  });

  it('should rename a fragment', function() {
    $httpBackend.expectPUT('rest/fragments/person/name', 'Persons').respond(200);

    fragmentRepo.rename('person', 'Persons');

    $httpBackend.flush();
  });

  it('should mark a fragment as favorite', function() {
    $httpBackend.expectPUT('rest/fragments/fragment-id/favorite', true).respond('');

    fragmentRepo.markAsFavorite('fragment-id');

    $httpBackend.flush();
  });

  it('should unmark a fragment as favorite', function() {
    $httpBackend.expectPUT('rest/fragments/fragment-id/favorite', false).respond('');

    fragmentRepo.unmarkAsFavorite('fragment-id');

    $httpBackend.flush();
  });

});
