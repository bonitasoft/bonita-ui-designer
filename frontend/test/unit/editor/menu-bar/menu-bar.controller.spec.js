(function() {

  'use strict';

  describe('Menu bar controller', function() {
    var pageRepo, scope, controller, $q, $window, $modal, modalInstance, $stateParams, $state;

    beforeEach(angular.mock.module('bonitasoft.designer.editor.menu-bar', 'mock.modal'));

    beforeEach(inject(function($rootScope, $controller, _pageRepo_, _$q_, _$modal_, $modalInstance, _$state_) {
      pageRepo = _pageRepo_;
      $q = _$q_;
      $window = {};
      $stateParams = {};
      scope = $rootScope;
      $modal = _$modal_;
      $state = _$state_;
      modalInstance = $modalInstance.fake();

      controller = $controller('MenuBarCtrl', {
        $window: $window,
        selectedResolution: {},
        $modal: $modal,
        $stateParams: $stateParams,
        $state: $state,
        artifactRepo: pageRepo,
        artifact: {},
        mode: 'page'
      });
    }));

    it('should save a page', function() {
      spyOn(pageRepo, 'save');
      var page = { id: 'person' };

      controller.save(page);

      expect(pageRepo.save).toHaveBeenCalledWith(page);
    });

    it('should save and export page', function() {
      spyOn(pageRepo, 'save').and.returnValue($q.when({}));
      spyOn(pageRepo, 'exportUrl').and.returnValue('export/page/person');
      var page = { id: 'person' };

      controller.saveAndExport(page);
      scope.$apply();

      expect(pageRepo.save).toHaveBeenCalledWith(page);
      expect($window.location).toBe('export/page/person');
    });

    it('should open help pop up', function() {
      spyOn($modal, 'open');

      controller.openHelp();

      expect($modal.open).toHaveBeenCalled();
      expect($modal.open.calls.mostRecent().args[0].templateUrl).toEqual('js/editor/menu-bar/help-popup.html');
      expect($modal.open.calls.mostRecent().args[0].size).toEqual('lg');
    });

    it('should save a page as ...', function() {
      spyOn($modal, 'open').and.returnValue(modalInstance);
      spyOn(pageRepo, 'create').and.returnValue($q.when({}));
      spyOn($state, 'go');
      var page = { id: 'person' };

      controller.saveAs(page);

      expect($modal.open).toHaveBeenCalled();
      expect($modal.open.calls.mostRecent().args[0].templateUrl).toEqual('js/editor/menu-bar/save-as-popup.html');
      expect($modal.open.calls.mostRecent().args[0].controller).toEqual('SaveAsPopUpController');

      modalInstance.close(page);
      scope.$apply();

      expect(pageRepo.create).toHaveBeenCalledWith(page, page.id);
      expect($state.go).toHaveBeenCalledWith($state.current, $stateParams, {
        reload: true
      });
    });
  });

})();
