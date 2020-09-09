describe('exposableData', function() {
  var element, $scope, yesInput, noInput;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.bottom-panel.data-panel'));
  beforeEach(angular.mock.module('bonitasoft.designer.templates'));

  beforeEach(inject(function($compile, $rootScope) {
    $scope = $rootScope.$new();
    element = $compile('<exposable-data model=\'exposed\'></exposable-data>')($scope);
    $scope.$apply();
    yesInput = element.find('#exposedYes');
    noInput = element.find('#exposedNo');
  }));

  it('should allow to expose a data', function() {
    $scope.exposed = false;
    $scope.$apply();

    yesInput.controller('ngModel').$setViewValue(yesInput.val() === 'true');
    $scope.$apply();

    expect($scope.exposed).toBeTruthy();
  });

  it('should allow to hide a data', function() {
    $scope.exposed = true;
    $scope.$apply();

    noInput.controller('ngModel').$setViewValue(noInput.val() === 'true');
    $scope.$apply();

    expect($scope.exposed).toBeFalsy();
  });
});
