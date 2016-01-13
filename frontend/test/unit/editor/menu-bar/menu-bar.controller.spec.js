(function() {

  'use strict';

  describe('Menu bar controller', function() {
    var pageRepo, scope, controller, $q, $window, $uibModal, modalInstance, $stateParams, $state, browserHistoryService;

    beforeEach(angular.mock.module('bonitasoft.designer.editor.header', 'mock.modal'));

    beforeEach(inject(function($rootScope, $controller, _pageRepo_, _$q_, _$uibModal_, $uibModalInstance, _$state_, _browserHistoryService_) {
      pageRepo = _pageRepo_;
      browserHistoryService = _browserHistoryService_;
      $q = _$q_;
      $window = {
        history: {
          back: jasmine.createSpy()
        }
      };
      $stateParams = {};
      scope = $rootScope;
      $uibModal = _$uibModal_;
      $state = _$state_;
      modalInstance = $uibModalInstance.fake();
      
      spyOn(browserHistoryService, 'back');
      spyOn($state, 'go');

      controller = $controller('EditorHeaderCtrl', {
        $window: $window,
        selectedResolution: {},
        $uibModal: $uibModal,
        $stateParams: $stateParams,
        $state: $state,
        artifactRepo: pageRepo,
        artifact: {},
        mode: 'page'
      });
    }));

    it('should navigate back', function() {
      controller.back();
      expect(browserHistoryService.back).toHaveBeenCalled();
      let fallback = browserHistoryService.back.calls.argsFor(0)[0];
      fallback();
      expect($state.go).toHaveBeenCalledWith('designer.home');
    });

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
      spyOn($uibModal, 'open');

      controller.openHelp();

      expect($uibModal.open).toHaveBeenCalled();
      expect($uibModal.open.calls.mostRecent().args[0].templateUrl).toEqual('js/editor/header/help-popup.html');
      expect($uibModal.open.calls.mostRecent().args[0].size).toEqual('lg');
    });

    it('should save a page as ...', function() {
      spyOn($uibModal, 'open').and.returnValue(modalInstance);
      spyOn(pageRepo, 'create').and.returnValue($q.when({}));
      var page = { id: 'person', type: 'page' };

      controller.saveAs(page);

      expect($uibModal.open).toHaveBeenCalled();
      expect($uibModal.open.calls.mostRecent().args[0].templateUrl).toEqual('js/editor/header/save-as-popup.html');
      expect($uibModal.open.calls.mostRecent().args[0].controller).toEqual('SaveAsPopUpController');

      modalInstance.close(page);
      scope.$apply();

      expect(pageRepo.create).toHaveBeenCalledWith(page, page.id);
      expect($state.go).toHaveBeenCalledWith('designer.page', $stateParams, {
        reload: true
      });
    });
  });

})();
