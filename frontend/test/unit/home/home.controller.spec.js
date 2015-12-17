describe('HomeCtrl', function() {
  var $scope, $q, artifactStore, controller, filter;
  var artifacts = [
    { id: 'page1', name: 'Page 1' },
    { id: 'widget1', name: 'Widget 1', custom: true, favorite: false },
    { id: 'page2', name: 'Page 2', favorite: true }];

  beforeEach(angular.mock.module('bonitasoft.designer.home'));
  beforeEach(inject(function($controller, $rootScope, $injector) {
    $scope = $rootScope.$new();
    $q = $injector.get('$q');
    artifactStore = $injector.get('artifactStore');
    filter = $injector.get('$filter')('filter');

    spyOn(artifactStore, 'load').and.returnValue($q.when(artifacts));

    controller = $controller('HomeCtrl', { $scope, artifactStore });
    $scope.$digest();
  }));

  it('should refresh everything', function() {

    $scope.refreshAll();
    $scope.$apply();

    expect($scope.artifacts).toEqual(artifacts);
  });

  it('should filter favorite artifact', function() {
    expect(filter(artifacts, $scope.filterFavoriteArtifact))
      .toEqual([
        jasmine.objectContaining({ id: 'page2' })
      ]);
  });

  it('should filter name on favorite artifact', function() {
    $scope.search = 'foobar';

    expect(filter(artifacts, $scope.filterFavoriteArtifact))
      .toEqual([]);
  });

  it('should exclude favorite artifact', function() {
    expect(filter(artifacts, $scope.excludeFavoriteArtifact))
      .toEqual([
        jasmine.objectContaining({ id: 'page1' }),
        jasmine.objectContaining({ id: 'widget1' })
      ]);
  });

  it('should filter name on not favorite artifact', function() {
    $scope.search = 'Wid';
    expect(filter(artifacts, $scope.excludeFavoriteArtifact))
      .toEqual([
        jasmine.objectContaining({ name: 'Widget 1' })
      ]);
  });

  it('should count favorite artifacts', function() {
    expect($scope.countFavoriteArtifact(artifacts)).toBe(1);
  });

  it('should count not favorite artifacts', function() {
    expect($scope.countNotFavoriteArtifact(artifacts)).toBe(2);
  });
});
