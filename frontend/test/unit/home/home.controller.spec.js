describe('HomeCtrl', function() {
  var $scope, $q, artifactStore, controller, filter;

  var pages = [
    { id: 'page1', name: 'Page 1', type: 'page' },
    { id: 'page2', name: 'Page 2', type: 'page', favorite: true }
  ];

  var widgets = [
    { id: 'widget1', name: 'Widget 1', type: 'widget', custom: true, favorite: false }
  ];

  var artifacts = [...pages, ...widgets];

  beforeEach(angular.mock.module('bonitasoft.designer.home'));
  beforeEach(inject(function($controller, $rootScope, $injector, $state) {
    $scope = $rootScope.$new();
    $q = $injector.get('$q');
    artifactStore = $injector.get('artifactStore');
    filter = $injector.get('$filter')('filter');

    spyOn(artifactStore, 'load').and.returnValue($q.when(artifacts));

    controller = $controller('HomeCtrl', { $scope, artifactStore });
    $scope.$digest();
    spyOn($state, 'href').and.callFake((state, params) => `${state}/${params.id}`);
  }));

  it('should refresh everything', function() {

    $scope.refreshAll();
    $scope.$apply();

    expect($scope.artifacts).toEqual({
      all: artifacts,
      page: pages,
      widget: widgets,
      form: [],
      layout: []
    });
    artifacts.forEach(artifact => expect(artifact.editionUrl).toBe(`designer.${artifact.type}/${artifact.id}`));
  });
});
