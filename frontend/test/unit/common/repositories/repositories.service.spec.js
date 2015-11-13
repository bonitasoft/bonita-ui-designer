describe('repositories service', () => {

  var repositories;

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories'));

  beforeEach(inject(function(_repositories_) {
    repositories = _repositories_;
  }));

  it('should create a repository', function() {

    var repository = repositories.create('aType', 'aBaseUrl');

    expect(repository.type).toBe('aType');
    expect(repository.baseUrl).toBe('aBaseUrl');
  });

  it('should retrieve a created repository by type', function() {

    var repository = repositories.create('aType', 'aBaseUrl');

    expect(repositories.get('aType')).toBe(repository);
  });
});
