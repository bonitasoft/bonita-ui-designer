describe('Import Success Message Controller', function() {

  var controller, scope;

  beforeEach(angular.mock.module('bonitasoft.designer.home.import'));
  beforeEach(inject(function($controller, $rootScope, gettextCatalog) {
    scope = $rootScope.$new();

    controller = $controller('ImportSuccessMessageController', {
      $scope: scope,
      gettextCatalog: gettextCatalog
    });
  }));

  it('should join artifacts on names', function() {
    var artifacts = [
      { name: 'first' },
      { name: 'second' }
    ];

    var names = controller.joinOnNames(artifacts);

    expect(names).toBe('first, second');
  });

  it('should get overwritten state when element has been overwritten', function() {
    scope.overwritten = true;

    var state = controller.getState();

    expect(state).toBe('overwritten');
  });

  it('should get added state when element has been added', function() {
    scope.overwritten = false;

    var state = controller.getState();

    expect(state).toBe('added');
  });
});
