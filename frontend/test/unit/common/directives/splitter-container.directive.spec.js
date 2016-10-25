describe('splitter container directive', function() {
  var $compile, element, scope, controller,$state;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));
  beforeEach(inject(function(_$compile_, _$rootScope_, _$state_) {
    $compile = _$compile_;
    $state = _$state_;
    scope = _$rootScope_.$new();
    element = $compile('<div splitter-container></div>')(scope);
    controller = element.controller('splitterContainer');

    scope.$digest();

    spyOn($state, 'go');
    $state.current = {
      name: 'stateTest'
    };
  }));

  describe('controller', function() {
    var scope;
    beforeEach(function() {
      scope = element.isolateScope();
    });

    it('should return if a given state is active', function() {
      expect(controller.isActive()).toBe(false);
      expect(controller.isActive('toto')).toBe(false);
      expect(controller.isActive('stateTest')).toBe(true);
    });

    it('should toggle a splitter-horizontal', function() {
      controller.toggle('stateTest');
      expect(controller.isOpen()).toBe(false);

      controller.toggle('stateTest');
      expect(controller.isOpen()).toBe(true);
      
      expect($state.go.calls.count()).toBe(2);
    });
  });
});
