describe('appSelectorDirective', function() {

  'use strict';

  var $localStorage, $httpBackend, appSelectorService, scope, $compile, vm, $scope;

  beforeEach(angular.mock.module('bonitasoft.designer.app-selector'));

  beforeEach(inject(function ($injector) {
    $localStorage = $injector.get('$localStorage');
    $httpBackend = $injector.get('$httpBackend');
    appSelectorService = $injector.get('appSelectorService');
    $compile = $injector.get('$compile');
    scope = $injector.get('$rootScope').$new();

    let appsResponse = {
      data: [
        {
          token: 'app1',
          appName: 'First Application'
        },
        {
          token: 'app2',
          appName: 'Second application'
        }
      ]
    };

    $httpBackend.expectGET('./API/living/application?preview=true&c=200').respond(appsResponse);

    scope.refreshIframe = jasmine.createSpy('refreshIframe');
    let element = $compile('<app-selector on-change="refreshIframe()"></app-selector>')(scope);

    scope.$apply();
    $scope = element.isolateScope();
    vm = $scope.vm;
  }));

  afterEach(function() {
    $localStorage.bonitaUIDesigner = {};
  });

  it('expect options to be available ', function(){
    spyOn(appSelectorService, 'getPathToLivingApp').and.returnValue(appSelectorService.getDefaultAppSelection().token);
    $httpBackend.flush();

    expect(vm.apps.data[0].token).toBe('app1');
    expect(vm.apps.data[1].token).toBe('app2');
  });

  it('sets a new app token', function() {
    $httpBackend.expectGET('./API/living/application?preview=true&c=1&p=0&f=token=app3').respond(
        [
          {
            displayName: 'First Application'
          }
        ]);

    spyOn(appSelectorService, 'getPathToLivingApp');
    $scope.setNewAppTokenAndRefresh('app3');

    $httpBackend.flush();

    expect(vm.selectedAppDisplayName).toBe('First Application');
    expect(scope.refreshIframe).toHaveBeenCalled();
  });

  it('sets the default app display name', function() {
    spyOn(appSelectorService, 'getPathToLivingApp');
    $scope.setNewAppTokenAndRefresh(appSelectorService.getDefaultAppSelection().token);

    expect(vm.selectedAppDisplayName).toBe('No application selected');
    expect(scope.refreshIframe).toHaveBeenCalled();
  });
});
