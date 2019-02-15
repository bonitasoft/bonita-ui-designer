describe('appSelectorDirective', function() {

  'use strict';

  var $localStorage, $httpBackend, appSelectorService, scope, $compile, vm, $scope, appsResponse, appSelector;

  beforeEach(angular.mock.module('bonitasoft.designer.app-selector'));
  beforeEach(inject(function ($injector) {
    $localStorage = $injector.get('$localStorage');
    $httpBackend = $injector.get('$httpBackend');
    appSelectorService = $injector.get('appSelectorService');
    $compile = $injector.get('$compile');
    scope = $injector.get('$rootScope').$new();

  }));

  describe('When LocalStorage contains an application', function() {
    beforeEach(inject(function() {
      spyOn(appSelectorService, 'getPathToLivingApp').and.returnValue('app1');
      appsResponse = [
        {
          token: 'app1',
          displayName: 'First Application',
          profileId: '4'
        },
        {
          token: 'app2',
          displayName: 'Second application',
          profileId: '4'
        },
        {
          token: 'app3',
          displayName: 'Third application',
          profileId: '2'
        }
      ];
      $httpBackend.expectGET('./API/living/application?preview=true&c=200').respond(appsResponse);
      $httpBackend.expectGET('./API/system/session/unusedId').respond({user_id: '4'});//jshint ignore:line
      $httpBackend.expectGET('./API/portal/profile?p=0&c=200&f=user_id%3d4').respond([{id: '4'}]);
      appSelector = $compile('<app-selector on-change="refreshIframe()"></app-selector>')(scope);

      scope.$apply();
      $scope = appSelector.isolateScope();
      vm = $scope.vm;
    }));

    it('should initialise the pathToLivingApp correctly', function() {
      expect(vm.pathToLivingApp).toBe('app1');
    });

  });


  describe('When LocalStorage is empty ', function () {

    beforeEach(inject(function () {

      appsResponse = [
        {
          token: 'app1',
          displayName: 'First Application',
          profileId: '4'
        },
        {
          token: 'app2',
          displayName: 'Second application',
          profileId: '4'
        },
        {
          token: 'app3',
          displayName: 'Third application',
          profileId: '2'
        }
      ];
      $httpBackend.expectGET('./API/living/application?preview=true&c=200').respond(appsResponse);
      $httpBackend.expectGET('./API/system/session/unusedId').respond({user_id: '4'});//jshint ignore:line
      $httpBackend.expectGET('./API/portal/profile?p=0&c=200&f=user_id%3d4').respond([{id: '4'}]);
      scope.refreshIframe = jasmine.createSpy('refreshIframe');
      appSelector = $compile('<app-selector on-change="refreshIframe()"></app-selector>')(scope);

      scope.$apply();
      $scope = appSelector.isolateScope();
      vm = $scope.vm;
    }));

    it('expect options to be available ', function () {
      spyOn(appSelectorService, 'getPathToLivingApp').and.returnValue(appSelectorService.getDefaultAppSelection().token);
      $httpBackend.flush();

      expect(vm.apps.length).toBe(2);
      expect(vm.apps[0].token).toBe('app1');
      expect(vm.apps[1].token).toBe('app2');
    });

    it('sets a new app token', function () {
      $httpBackend.flush();
      $httpBackend.expectGET('./API/living/application?preview=true&c=1&p=0&f=token=app4').respond([
        {
          displayName: 'Fourth Application'
        }
      ]);

      spyOn(appSelectorService, 'getPathToLivingApp');
      $scope.setNewAppTokenAndRefresh('app4');

      $httpBackend.flush();

      expect(vm.selectedAppDisplayName).toBe('Fourth Application');
      expect(scope.refreshIframe).toHaveBeenCalled();
    });

    it('sets the default app display name', function () {
      spyOn(appSelectorService, 'getPathToLivingApp');
      $scope.setNewAppTokenAndRefresh(appSelectorService.getDefaultAppSelection().token);

      expect(vm.selectedAppDisplayName).toBe('No application selected');
      expect(scope.refreshIframe).toHaveBeenCalled();
    });
  });

  describe('When LocalStorage contains an out off date application ', function () {
    beforeEach(inject(function () {

      appsResponse = [];
      $httpBackend.expectGET('./API/living/application?preview=true&c=200').respond(appsResponse);
      $httpBackend.expectGET('./API/system/session/unusedId').respond({user_id: '4'});//jshint ignore:line
      $httpBackend.expectGET('./API/portal/profile?p=0&c=200&f=user_id%3d4').respond([{id: '4'}]);
      spyOn(appSelectorService, 'getPathToLivingApp').and.returnValue('app1');
      scope.refreshIframe = jasmine.createSpy('refreshIframe');
      appSelector = $compile('<app-selector on-change="refreshIframe()"></app-selector>')(scope);

      scope.$apply();
      $scope = appSelector.isolateScope();
      vm = $scope.vm;
    }));

    it('set the default app display name ', function () {
      $httpBackend.flush();

      expect(vm.selectedAppDisplayName).toBe('No application selected');
    });
  });
});
