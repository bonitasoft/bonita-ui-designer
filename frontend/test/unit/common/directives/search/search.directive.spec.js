describe('Search', () => {

  var element, $scope, $timeout;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));

  beforeEach(inject(function($compile, $rootScope, _$timeout_) {
    $scope = $rootScope.$new();
    $timeout = _$timeout_;
    $scope.placeholder = 'foobar';
    element = $compile(`<form name="form"><search value="search" placeholder="{{ placeholder }}"></search></form>`)($scope);
    $scope.$apply();
  }));

  describe('directive', () => {

    it('should update value', function() {

      $scope.form.search.$setViewValue('abc');

      $timeout(() => expect($scope.search).toBe('abc'));
    });

    it('should clear value when clicking on clear button', function() {
      $scope.form.search.$setViewValue('abc');

      element.find('.Search-clearButton').click().trigger('click');

      $timeout(() => expect($scope.search).toBe(''));
    });

    it('should display a placeholder', function() {
      expect(element.find('.Search-input').attr('placeholder')).toBe('foobar');
    });
  });
});
