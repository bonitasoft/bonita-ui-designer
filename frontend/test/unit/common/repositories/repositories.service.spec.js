describe('repositories service', () => {

  var repositories;

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories'));

  beforeEach(inject(function(_repositories_) {
    repositories = _repositories_;
  }));

  it('should create a repository', function() {
    var repository = {};
    expect(repositories.add('aType', repository)).toBe(repository);

    expect(repositories.get('aType')).toBe(repository);
  });
});
