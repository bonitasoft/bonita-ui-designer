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

});
