describe('PropertyEditorPopupCtrl', function() {

  var $scope, modalInstance;

  beforeEach(module('bonitasoft.designer.custom-widget', 'mock.modal'));

  beforeEach(inject(function($rootScope, $controller, $timeout, $modalInstance) {
    $scope = $rootScope.$new();
    modalInstance = $modalInstance.create();

    $controller('PropertyEditorPopupCtrl', {
      $scope: $scope,
      $modalInstance:  modalInstance,
      param: {type: 'page'},
      $timeout: $timeout
    });
    $timeout.flush();
  }));

  it('should close the popup and pass params', function() {
    $scope.currentParam = {name: "toto"};
    $scope.paramToUpdate = {name: "titi"};
    var params = {
      param: $scope.currentParam,
      paramToUpdate: $scope.paramToUpdate
    };

    $scope.ok();
    expect(modalInstance.close).toHaveBeenCalledWith(params);
  });

  it('should dismiss the popup', function() {
    $scope.cancel();
    expect(modalInstance.dismiss).toHaveBeenCalledWith('cancel');
  });

  it('should expose popup types to scope', function() {
    expect(Array.isArray($scope.types)).toBe(true);
  });

  it('should expose currentParam to scope', function() {
    expect(angular.isObject($scope.currentParam)).toBe(true);
  });
});
