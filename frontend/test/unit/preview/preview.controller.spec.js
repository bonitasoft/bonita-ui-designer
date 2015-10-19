describe('PreviewCtrl', function() {
  var ctrl, $scope, $q, $location, $stateParams, iframeParameters, webSocket, pageRequest, pageRepo, clock, $timeout;

  beforeEach(angular.mock.module('bonitasoft.designer.preview', 'mock.webSocket'));

  beforeEach(inject(function($injector, _webSocket_) {

    $q = $injector.get('$q');
    $scope = $injector.get('$rootScope').$new();
    $location = $injector.get('$location');
    $stateParams = $injector.get('$stateParams');
    $timeout = $injector.get('$timeout');

    pageRequest = $q.defer();

    pageRepo = { load: angular.noop };
    spyOn(pageRepo, 'load').and.returnValue(pageRequest.promise);

    webSocket = _webSocket_;
    clock = {
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
    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/1337/?time=now');
    spyOn(clock, 'now').and.returnValue('newNow');

    webSocket.send('/previewableUpdates', '1337');
    $scope.$apply();

    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/1337/?time=newNow');
  });

  it('should not update the iframe src when a notification is received for another id', function() {
    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/1337/?time=now');

    webSocket.send('/previewableUpdates', 'notgoodid');
    $scope.$apply();

    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/1337/?time=now');
  });

  it('should refresh preview iframe', function() {
    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/1337/?time=now');
    spyOn(clock, 'now').and.returnValue('newNow');

    $scope.refreshIframe();

    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/1337/?time=newNow');
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
