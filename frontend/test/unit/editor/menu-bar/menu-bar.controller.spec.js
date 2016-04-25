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

    it('should check if page is dirty or pristine', () => {
      expect(controller.pristine).toBeTruthy();
      expect(controller.dirty).toBeFalsy();
      expect(controller.isPageDirty({})).toBeFalsy();
      var page = { id: 'person' };
      expect(controller.isPageDirty(page)).toBeTruthy();
      expect(controller.pristine).toBeFalsy();
      expect(controller.dirty).toBeTruthy();
    });

    it('should save a page', function() {
      spyOn(pageRepo, 'save').and.returnValue($q.when({}));
      var page = { id: 'person' };
      controller.dirty = true;

      controller.save(page);
      scope.$apply();

      expect(controller.dirty).toBeFalsy();
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
      spyOn(controller, 'removeReferences').and.callThrough();
      var page = { id: 'person', type: 'page' };

      controller.saveAs(page);

      expect($uibModal.open).toHaveBeenCalled();
      expect($uibModal.open.calls.mostRecent().args[0].templateUrl).toEqual('js/editor/header/save-as-popup.html');
      expect($uibModal.open.calls.mostRecent().args[0].controller).toEqual('SaveAsPopUpController');

      modalInstance.close(page);
      scope.$apply();

      expect(pageRepo.create).toHaveBeenCalledWith(page, page.id);
      expect(controller.removeReferences).toHaveBeenCalledWith(page);
      expect($state.go).toHaveBeenCalledWith('designer.page', $stateParams, {
        reload: true
      });
    });

    it('should remove reference attribute from every item', () => {
      expect(controller.removeReferences({})).toEqual({});
      expect(controller.removeReferences({ reference: 'test' })).toEqual({});
      expect(controller.removeReferences([{ reference: 'test' }])).toEqual([{}]);
      expect(controller.removeReferences({ rows: [{ reference: 'test' }] })).toEqual({ rows: [{}] });
      let page = {
        'name': 'test3',
        'reference': 'bonita',
        'rows': [ [{ 'type': 'fragment', 'reference': '14bb674b-06ee-40e2-9639-432f0337937a', 'id': '6c959a04-a8a8-4fde-b8d8-b76323cd1629' }] ],
        'inactiveAssets': [], 'data': {}
      };
      page.rows[0].$$parentContainer = page;
      //using angular json conversion allow to remove properties starting with a $
      //avoiding a circular reference to page
      let result = angular.fromJson(angular.toJson(controller.removeReferences(page)));
      expect(result).toEqual({
        'name': 'test3',
        'rows': [ [{ 'type': 'fragment', 'id': '6c959a04-a8a8-4fde-b8d8-b76323cd1629', }] ],
        'inactiveAssets': [], 'data': {} });
    });
  });
})();
