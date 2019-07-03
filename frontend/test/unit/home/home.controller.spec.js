describe('HomeCtrl', function() {
  var $scope, $q, artifactStore, controller, filter;

  var pages = [
    { id: 'page1', name: 'Page 1', type: 'page' },
    { id: 'page2', name: 'Page 2', type: 'page', favorite: true }
  ];

  var widgets = [
    { id: 'widget1', name: 'Widget 1', type: 'widget', custom: true, favorite: false }
  ];

  var fragments = [];

  var artifacts = [...pages, ...widgets, ...fragments];

  beforeEach(angular.mock.module('bonitasoft.designer.home', 'bonitasoft.designer.editor.whiteboard'));
  beforeEach(inject(function($controller, $rootScope, $injector, $state) {
    $scope = $rootScope.$new();
    $q = $injector.get('$q');
    artifactStore = $injector.get('artifactStore');
    filter = $injector.get('$filter')('filter');

    spyOn(artifactStore, 'loadRepository').and.returnValue($q.when(artifacts));
    spyOn(artifactStore, 'load').and.returnValue($q.when(artifacts));

    controller = $controller('HomeCtrl', { $scope, artifactStore });
    $scope.$digest();
    spyOn($state, 'href').and.callFake((state, params) => `${state}/${params.id}`);
  }));

  it('should refresh everything', function() {

    $scope.refreshAll();
    $scope.$apply();

    expect($scope.$storage.homeSortOrder).toEqual('-lastUpdate');

    expect($scope.artifacts).toEqual({
      all: artifacts,
      page: pages,
      widget: widgets,
      form: [],
      layout: []
    });
    artifacts.forEach(artifact => expect(artifact.editionUrl).toBe(`designer.${artifact.type}/${artifact.id}`));
  });

  it('should find type from activated tab', () => {
    expect($scope.getActivatedArtifact()).toBeUndefined();
    $scope.types[1].active = true;
    expect($scope.getActivatedArtifact()).toBe($scope.types[1]);
    $scope.types[3].active = true;
    $scope.types[1].active = false;
    expect($scope.getActivatedArtifact()).toBe($scope.types[3]);
  });
});
