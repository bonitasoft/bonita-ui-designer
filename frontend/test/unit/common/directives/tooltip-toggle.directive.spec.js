describe('tooltip toggle', function() {
  var element, $scope, $compile, $timeout,  handlers, rootScope;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));
  beforeEach(inject(function($injector) {
    rootScope = $injector.get('$rootScope');
    $compile = $injector.get('$compile');
    $timeout = $injector.get('$timeout');
  }));

  beforeEach(function() {
    $scope = rootScope.$new();

    handlers = {
      show: jasmine.createSpy('show'),
      hide: jasmine.createSpy('hide')
    };

    element = $compile('<div tooltip-toggle="expression">')($scope);
    element.on('show-tooltip', handlers.show);
    element.on('hide-tooltip', handlers.hide);

  });

  it('should call show handler when expression is true', function() {
    $scope.expression = true;
    rootScope.$digest();
    $timeout.flush();
    expect(handlers.show).toHaveBeenCalled();
  });

  it('should call show handler when expression is false', function() {
    $scope.expression = false;
    rootScope.$digest();
    $timeout.flush();
    expect(handlers.hide).toHaveBeenCalled();
  });

});
