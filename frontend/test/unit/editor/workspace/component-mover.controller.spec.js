describe('ComponentMoverDirectiveCtrl', function() {
  var $scope;
  var component1;
  var component2;
  var component3;
  var row;

  beforeEach(module('bonitasoft.ui.directives', 'bonitasoft.ui.common.services'));
  beforeEach(inject(function($rootScope, $controller) {
    $scope = $rootScope.$new();

    component1 = {c1: true};
    component2 = {c2: true};
    component3 = {c3: true};
    $scope.component = component2;
    row = [component1, component2, component3];
    angular.forEach(row, function(component) {
      component.$$parentContainerRow = {
        row: row
      }
    });

    $controller('ComponentMoverDirectiveCtrl', {
      $scope: $scope
    });
  }));

  it('should move the component left', function() {
    expect($scope.moveLeftVisible()).toBeTruthy();
    $scope.moveLeft();
    expect(row[0]).toBe($scope.component);
    expect(row[1]).toBe(component1);
    expect($scope.moveLeftVisible()).toBeFalsy();
  });

  it('should move the component right', function() {
    expect($scope.moveRightVisible()).toBeTruthy();
    $scope.moveRight();
    expect(row[1]).toBe(component3);
    expect(row[2]).toBe($scope.component);
    expect($scope.moveRightVisible()).toBeFalsy();
  });
});
