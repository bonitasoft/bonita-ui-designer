describe('directive openPreview', function() {

  'use strict';

  var scope, $window, $state, $q, resolutions, dom, $compile, controller;

  beforeEach(angular.mock.module('bonitasoft.designer.preview', 'bonitasoft.designer.templates'));
  beforeEach(inject(function($injector) {

    $q = $injector.get('$q');
    $window = $injector.get('$window');
    $state = $injector.get('$state');
    scope = $injector.get('$rootScope').$new();
    resolutions = $injector.get('resolutions');
    $compile = $injector.get('$compile');

    spyOn($window, 'open').and.returnValue(jasmine.createSpyObj('window', ['focus']));
    spyOn($state, 'href').and.returnValue('/preview?resolution=xs');
    spyOn(resolutions, 'selected').and.returnValue({ key: 'xs' });
  }));

  describe('directive default behaviour', function() {

    beforeEach(function () {
      scope.vm = {
        page: {id: '12345'},
        mode: 'page',
        isValid: true,
        save: (() => {
          var deferred = $q.defer();
          deferred.resolve();
          return deferred.promise;
        })
      };
      spyOn(scope.vm, 'save').and.callThrough();
      dom = $compile('<open-preview on-open-preview="vm.save(vm.page)" mode="{{vm.mode}}" is-disabled="!vm.isValid" artifact-id="vm.page.id"></open-preview>')(scope);
      scope.$apply();
      controller = dom.controller('openPreview');
    });

    it('should create a new pop up when it has not been created yet', function () {
      expect(controller.previewWindow).toBeUndefined();

      dom.find('button').click();

      expect($state.href).toHaveBeenCalledWith('designer.preview', {resolution: 'xs', id: '12345', mode: 'page'});
      expect($window.open).toHaveBeenCalledWith('/preview?resolution=xs', 'preview', 'width=1024,height=768,toolbar=1,resizable=1,scrollbars=1');
      expect(scope.vm.save).toHaveBeenCalledWith(scope.vm.page);
    });

    it('should create a new pop up when it has been closed', function () {
      dom.find('button').click();

      controller.previewWindow = { closed: true };
      dom.find('button').click();

      expect($window.open.calls.count()).toBe(2);
    });

    it('should focus the pop up when it has already been created', function () {

      dom.find('button').click();
      dom.find('button').click();

      expect($window.open.calls.count()).toBe(1);
      expect(controller.previewWindow.focus).toHaveBeenCalled();
    });

  });
});
