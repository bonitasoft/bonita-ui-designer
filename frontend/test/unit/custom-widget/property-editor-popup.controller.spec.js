describe('PropertyEditorPopupCtrl', function() {

  var $scope, modalInstance;

  beforeEach(angular.mock.module('bonitasoft.designer.custom-widget', 'mock.modal'));

  beforeEach(inject(function($rootScope, $controller, $timeout, $uibModalInstance) {
    $scope = $rootScope.$new();
    modalInstance = $uibModalInstance.create();

    $controller('PropertyEditorPopupCtrl', {
      $scope: $scope,
      $uibModalInstance:  modalInstance,
      param: { type: 'page' }
    });
  }));

  it('should close the popup and pass params', function() {
    $scope.currentParam = { name: 'toto', defaultValue: 'default value' };
    $scope.paramToUpdate = { name: 'titi' };
    var params = {
      param: $scope.currentParam,
      paramToUpdate: $scope.paramToUpdate
    };

    $scope.ok();
    expect($scope.currentParam.defaultValue).toEqual('default value');
    expect(modalInstance.close).toHaveBeenCalledWith(params);
  });

  it('should close the popup and pass params and reset default value', function() {
    $scope.selectedBond = 'variable';
    $scope.currentParam = { name: 'toto', defaultValue: 'default value' };
    $scope.paramToUpdate = { name: 'titi' };
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
    $scope.updateBond('interpolation');
    expect($scope.currentParam.bond).toEqual('interpolation');
    expect($scope.currentParam.type).toEqual('text');
    $scope.currentParam.type = 'choice';
    $scope.updateBond('expression');
    expect($scope.currentParam.bond).toEqual('expression');
    expect($scope.currentParam.type).toEqual('choice');
  });

  it('should expose popup types to scope', function() {
    expect(Array.isArray($scope.types)).toBe(true);
  });

  it('should expose currentParam to scope', function() {
    expect(angular.isObject($scope.currentParam)).toBe(true);
  });

  describe('isTypeChoicable', function() {
    it('should return false when type is not choice', function() {
      $scope.currentParam.type = 'bonita';
      expect($scope.isTypeChoicable()).toBeFalsy();
    });
    it('should return false when type is choice and selected bond is variable or interpolation', function() {
      $scope.currentParam.bond = 'variable';
      $scope.currentParam.type = 'choice';
      expect($scope.isTypeChoicable()).toBeFalsy();
      $scope.currentParam.bond = 'interpolation';
      expect($scope.isTypeChoicable()).toBeFalsy();
    });
    it('should return true when selected bond is expression or constant and choice is type', function() {
      $scope.currentParam.bond = 'expression';
      $scope.currentParam.type = 'choice';
      expect($scope.isTypeChoicable()).toBeTruthy();
      $scope.currentParam.bond = 'constant';
      expect($scope.isTypeChoicable()).toBeTruthy();
    });
  });

  describe('isTypeSelectable', function() {
    it('should return false when selected bond is variable or interpolation', function() {
      $scope.currentParam.bond = 'variable';
      expect($scope.isTypeSelectable()).toBeFalsy();
      $scope.currentParam.bond = 'interpolation';
      expect($scope.isTypeSelectable()).toBeFalsy();
    });
    it('should return true when selected bond is expression or constant', function() {
      $scope.currentParam.bond = 'expression';
      expect($scope.isTypeSelectable()).toBeTruthy();
      $scope.currentParam.bond = 'constant';
      expect($scope.isTypeSelectable()).toBeTruthy();
    });
  });

});
