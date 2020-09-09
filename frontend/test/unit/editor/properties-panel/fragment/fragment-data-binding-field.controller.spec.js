describe('fragment data binding field controller', function() {
  var $scope;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.properties-panel'));
  beforeEach(inject(function($rootScope, $controller) {
    $scope = $rootScope.$new();

    $controller('FragmentDataBindingFieldDirectiveCtrl', {
      $scope: $scope
    });
  }));

  it('should initialize binding if undefined', function() {
    expect($scope.binding).toEqual('');
  });

});
