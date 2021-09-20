describe('PreviewCtrl', function() {
  let $scope, $q, $location, iframeParameters, webSocket, pageRequest, configuration, pageRepo, clock, $log, $http, $window, $httpBackend;

  beforeEach(angular.mock.module('bonitasoft.designer.preview', 'mock.webSocket', 'bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function($injector, _webSocket_) {

    $q = $injector.get('$q');
    $scope = $injector.get('$rootScope').$new();
    $scope.pathToLivingApp = 'no-app-selected';
    $location = $injector.get('$location');
    $window = $injector.get('$window');
    $log = $injector.get('$log');
    $http = $injector.get('$http');
    configuration = $injector.get('configuration');
    pageRepo = $injector.get('pageRepo');
    pageRequest = $q.defer();
    $httpBackend = $injector.get('$httpBackend');
    spyOn(pageRepo, 'load').and.returnValue(pageRequest.promise);
    spyOn($window, 'close');

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

    $injector.get('$controller')('PreviewCtrl', {
      $scope,
      $location,
      $log,
      $http,
      configuration,
      $window,
      iframeParameters,
      webSocket,
      clock,
      artifactRepo: pageRepo,
      mode: 'page'
    });

    $httpBackend.whenGET('./rest/config').respond(200, {appServerUrl:'http://localhost:4200'});
    $httpBackend.whenGET('./API/living/application?preview=true&c=200').respond(200, []);
    $httpBackend.whenGET('/preview/page/no-app-selected/1337/?time=now').respond(200, 'ok');

    webSocket.resolveConnection();
    $scope.$apply();

  }));

  it('should set the iframe\'s source', function () {
    // then $sce should build a value for the iframe src, that we have to unwrap
    initTest('2.0');
    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/no-app-selected/1337/?time=now');
  });

  it('should set the iframe\'s source for a V3 page', function() {
    // then $sce should build a value for the iframe src, that we have to unwrap
    initTest('3.0');
    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('http://localhost:4200');
  });

  it('should set the iframe\'s source with additionnal query parameters', function() {
    spyOn($location, 'search').and.returnValue({ app: 'myApp', locale: 'fr' });

    $scope.refreshIframe();

    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/no-app-selected/1337/?app=myApp&locale=fr&time=now');
  });

  it('should update the iframe src when a notification is received', function() {
    initTest('2.0');
    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/no-app-selected/1337/?time=now');
    spyOn(clock, 'now').and.returnValue('newNow');

    webSocket.send('/previewableUpdates', '1337');
    $scope.$apply();

    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/no-app-selected/1337/?time=newNow');
  });

  it('should not update the iframe src when a notification is received for another id', function() {
    initTest('2.0');
    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/no-app-selected/1337/?time=now');

    webSocket.send('/previewableUpdates', 'notgoodid');
    $scope.$apply();

    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/no-app-selected/1337/?time=now');
  });

  it('should refresh preview iframe', function() {
    initTest('2.0');
    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/no-app-selected/1337/?time=now');
    spyOn(clock, 'now').and.returnValue('newNow');

    $scope.refreshIframe();

    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/no-app-selected/1337/?time=newNow');
  });

  it('should load the page name onLoad', function() {
    expect(pageRepo.load).toHaveBeenCalledWith('1337');
  });

  it('should fill the scope.pageName', function() {
    expect($scope.pageName).toBeUndefined();
    initTest('2.0');
    expect($scope.pageName).toBe('jeanne');
  });

  it('should close the window when a notification is received', function() {
    initTest('2.0');
    expect($scope.iframe.src.$$unwrapTrustedValue()).toBe('/preview/page/no-app-selected/1337/?time=now');
    spyOn(clock, 'now').and.returnValue('newNow');

    webSocket.send('/previewableRemoval', '1337');
    $scope.$apply();

    expect($window.close).toHaveBeenCalled();
  });

  function initTest(modelVersion) {
    pageRequest.resolve({
      data: {
        name: 'jeanne',
        modelVersion: modelVersion
      }
    });
    $scope.$apply();
    $httpBackend.flush();
  }
});
