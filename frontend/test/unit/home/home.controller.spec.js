describe('HomeCtrl', function() {
  var $scope, $q, artifactStore, controller;
  var artifacts = [{ id: 'page1', name: 'Page 1' }, { id: 'widget1', name: 'Widget 1', custom: true }];

  beforeEach(angular.mock.module('bonitasoft.designer.home'));
  beforeEach(inject(function($controller, $rootScope, $injector) {
    $scope = $rootScope.$new();
    $q = $injector.get('$q');
    artifactStore = $injector.get('artifactStore');

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
