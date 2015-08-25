describe('PropertyEditorPopupCtrl', function() {

  var $scope, modalInstance;

  beforeEach(module('bonitasoft.designer.custom-widget', 'mock.modal'));

  beforeEach(inject(function($rootScope, $controller, $timeout, $modalInstance) {
    $scope = $rootScope.$new();
    modalInstance = $modalInstance.create();

    $controller('PropertyEditorPopupCtrl', {
      $scope: $scope,
      $modalInstance:  modalInstance,
      param: {type: 'page'}
    });
  }));

  it('should close the popup and pass params', function() {
    $scope.currentParam = {name: "toto", defaultValue: 'default value'};
    $scope.paramToUpdate = {name: "titi"};
    var params = {
      param: $scope.currentParam,
      paramToUpdate: $scope.paramToUpdate
    };

    $scope.ok();
    expect(modalInstance.close).toHaveBeenCalledWith(params);
  });

  it('should close the popup and pass params and reset default value', function() {
    $scope.selectedBond.name = 'variable';
    $scope.currentParam = {name: 'toto', defaultValue: 'default value'};
    $scope.paramToUpdate = {name: 'titi'};
    var params = {
      param: $scope.currentParam,
      paramToUpdate: $scope.paramToUpdate
    };

    $scope.ok();
    expect($scope.currentParam.defaultValue).toBeFalsy();
    expect(modalInstance.close).toHaveBeenCalledWith(params);
  });

  it('should dismiss the popup', function() {
    $scope.cancel();
    expect(modalInstance.dismiss).toHaveBeenCalledWith('cancel');
  });

  it('should update type accordingly to selected bond', function() {
    $scope.currentParam.type = 'choice';
    $scope.selectedBond = {type: 'text'};
    $scope.updateType();
    expect($scope.currentParam.type).toEqual('text');
    $scope.currentParam.type = 'choice';
    $scope.selectedBond = {};
    $scope.updateType();
    expect($scope.currentParam.type).toEqual('choice');
  });

  it('should expose popup bonds to scope', function() {
    expect(Array.isArray($scope.bonds)).toBe(true);
    expect($scope.selectedBond).toEqual({name:'expression'});
  });

  it('should expose popup types to scope', function() {
    expect(Array.isArray($scope.types)).toBe(true);
  });

  it('should expose currentParam to scope', function() {
    expect(angular.isObject($scope.currentParam)).toBe(true);
  });
});
