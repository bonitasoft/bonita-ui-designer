describe('home filters', () => {

  var element, $scope;

  beforeEach(angular.mock.module('bonitasoft.designer.home'));

  beforeEach(inject(function ($compile, $rootScope) {
    $scope = $rootScope.$new();
    $scope.filters = {};
    element = $compile('<home-filters filters="filters"></home-filters>')($scope);
    $scope.$apply();
  }));

  describe('directive', () => {

    it('should toggle favorite filter', function() {
      element.find('input[name="favorites"]').click().trigger('click');
      expect($scope.filters.favorite).toBe(true);

      element.find('input[name="all"]').click().trigger('click');
      expect($scope.filters.favorite).toBeUndefined();
    });
  });
});
