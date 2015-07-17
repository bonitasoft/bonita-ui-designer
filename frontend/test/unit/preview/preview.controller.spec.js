describe('PreviewCtrl', function() {
  var ctrl, $scope, $q, $location, $stateParams, iframeParameters, webSocket, pageRequest, pageRepo;


  beforeEach(module('bonitasoft.ui.preview'));

  beforeEach(inject(function ($injector) {

    $q = $injector.get('$q');
    $scope = $injector.get('$rootScope').$new();
    $location = $injector.get('$location');
    $stateParams = $injector.get('$stateParams');

    pageRequest = $q.defer();

    pageRepo = {load: angular.noop};
    spyOn(pageRepo, 'load').and.returnValue(pageRequest.promise);

    webSocket = jasmine.createSpyObj('webSocket', ['listen']);
    var clock = {
      now: function() {
        return 'now';
      }
    };

    // given the following iframe properties:
    iframeParameters = {
      url: '/preview/page',
      id: '1337'
    };

    ctrl = $injector.get('$controller')('PreviewCtrl', {
      $scope: $scope,
      iframeParameters: iframeParameters,
      webSocket: webSocket,
      artifactRepo: pageRepo,
      $stateParams: $stateParams,
      clock: clock
    });

  }));

  it('should set the iframe\'s source', function() {
    // then $sce should build a value for the iframe src, that we have to unwrap
    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/1337/?time=now');
  });

  it('should update the iframe src when a notification is received', function() {
    $scope.iframe.src = '';

    ctrl.wsCallback(iframeParameters.id);

    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/1337/?time=now');
  });

  it('should not update the iframe src when a notification is received for another id', function() {
    $scope.iframe.src = '';

    ctrl.wsCallback('person');

    expect($scope.iframe.src).toBe('');
  });

  it('should load the page name onLoad', function() {
    expect(pageRepo.load).toHaveBeenCalledWith('1337');
  });

  it('should fill the scope.pageName', function() {
    expect($scope.pageName).toBeUndefined();
    pageRequest.resolve({
      data: {
        name: 'jeanne'
      }
    });
    $scope.$apply();
    expect($scope.pageName).toBe('jeanne');
  });
});
