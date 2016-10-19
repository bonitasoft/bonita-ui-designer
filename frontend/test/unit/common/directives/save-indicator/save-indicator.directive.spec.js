describe('SaveIndicator', () => {

  var indicator, $scope, $timeout;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));

  beforeEach(inject(function ($compile, $rootScope, _$timeout_) {
    $scope = $rootScope.$new();
    $timeout = _$timeout_;

    let element = $compile(`<save-indicator></save-indicator>`)($scope);
    $scope.$apply();
    indicator = element.find('.SaveIndicator');
  }));

  it('should be hidden by default', function () {
    expect(indicator.hasClass('SaveIndicator--visible')).toBeFalsy();
  });

  it('should blink for one second while saved event is triggered', () => {
    $scope.$broadcast('saved');
    $scope.$apply();
    expect(indicator.hasClass('SaveIndicator--visible')).toBeTruthy();

    $timeout.flush();
    expect(indicator.hasClass('SaveIndicator--visible')).toBeFalsy();
  });
});
