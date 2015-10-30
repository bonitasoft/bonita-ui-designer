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

  it('should get overridden state when element has been overridden', function() {
    scope.overridden = true;

    var state = controller.getState();

    expect(state).toBe('overridden');
  });

  it('should get added state when element has been added', function() {
    scope.overridden = false;

    var state = controller.getState();

    expect(state).toBe('added');
  });
});
