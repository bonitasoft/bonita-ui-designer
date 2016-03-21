describe('DeletionPopController', function() {
  var page, modalInstance, $scope;

  beforeEach(angular.mock.module('bonitasoft.designer.home'));
  beforeEach(inject(function($controller, $rootScope) {
    $scope = $rootScope.$new();

    page = {
      id: 'page1', name: 'page1', usedBy: {
        page: [{ type: 'page' }, { type: 'layout' }, { type: 'layout' }],
        foobar: [{ type: 'foobar' }]
      }
    };
    modalInstance = jasmine.createSpyObj('modalInstance', ['close', 'dismiss']);

    $controller('DeletionPopUpController', {
      $scope: $scope,
      $uibModalInstance: modalInstance,
      artifact: page,
      type: 'page'
    });

    $scope.$apply();
  }));

  it('should close the modal instance', function() {
    $scope.ok();

    expect(modalInstance.close).toHaveBeenCalledWith(page.id);
  });

  it('should dismiss the modal instance', function() {
    $scope.cancel();

    expect(modalInstance.dismiss).toHaveBeenCalledWith('cancel');
  });

  it('should populate artifact and his type', function() {
    expect($scope.artifact).toEqual(page);
    expect($scope.artifact.type).toBe('page');
  });

  it('should list elements which use artifact', function() {
    expect($scope.usedBy).toEqual({
      page: [{ type: 'page' }],
      layout: [{ type: 'layout' }, { type: 'layout' }],
      foobar: [{ type: 'foobar' }]
    });
  });
});
