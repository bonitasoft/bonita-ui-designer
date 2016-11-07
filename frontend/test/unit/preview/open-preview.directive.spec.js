describe('directive openPreview', function() {

  'use strict';

  var scope, $window, $state, resolutions, dom, $compile, controller;

  beforeEach(angular.mock.module('bonitasoft.designer.preview', 'bonitasoft.designer.templates'));
  beforeEach(inject(function($injector) {

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

    beforeEach(function() {
      scope.vm = {
        page: { id: '12345' },
        mode: 'page',
        isValid: true,
        save: jasmine.createSpy()
      };
      dom = $compile('<open-preview on-open-preview="vm.save(vm.page)" mode="{{vm.mode}}" is-disabled="!vm.isValid" artifact-id="vm.page.id"></open-preview>')(scope);
      scope.$apply();
      controller = dom.controller('openPreview');
    });

    it('should try to get the current preview url', function() {
      expect(controller.previewWindow).toBeUndefined();
      dom.find('button').click();
      expect($state.href).toHaveBeenCalledWith('designer.preview', { resolution: 'xs', id: '12345', mode: 'page' });
      expect($window.open).toHaveBeenCalledWith('/preview?resolution=xs', 'preview', 'width=1024,height=768,resizable=1,scrollbars=1');
      expect(scope.vm.save).toHaveBeenCalledWith(scope.vm.page);
      dom.find('button').click();
      expect(controller.previewWindow.focus).toHaveBeenCalled();
    });

    it('should open a popup', function() {
      scope.vm.mode = 'fragment';
      dom = $compile('<open-preview on-open-preview="vm.save(vm.page)" mode="{{vm.mode}}" is-disabled="!vm.isValid" artifact-id="vm.page.id"></open-preview>')(scope);
      scope.$apply();
      dom.find('button').click();
      expect($state.href).toHaveBeenCalledWith('designer.preview', { resolution: 'xs', id: '12345', mode: 'fragment' });
    });

  });
});
