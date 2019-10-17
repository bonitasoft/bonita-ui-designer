(function () {

  'use strict';

  describe('Header controller', function () {
    var pageRepo, scope, controller, $q, $window, $uibModal, modalInstance, $stateParams, $state, $localStorage,
      browserHistoryService, artifactStore, artifactNamingValidatorService, dataManagementRepo;

    beforeEach(angular.mock.module('bonitasoft.designer.editor.header', 'mock.modal', 'bonitasoft.designer.editor.whiteboard', 'bonitasoft.designer.home'));

    beforeEach(inject(function ($rootScope, $controller, _pageRepo_, _$q_, _$uibModal_, _$localStorage_, $uibModalInstance, _$state_, _browserHistoryService_, _artifactStore_, _artifactNamingValidatorService_, _dataManagementRepo_) {
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
      $localStorage = _$localStorage_;
      $localStorage.bonitaUIDesigner = {};
      modalInstance = $uibModalInstance.fake();

      spyOn(browserHistoryService, 'back');
      spyOn($state, 'go');

      artifactStore = _artifactStore_;
      artifactNamingValidatorService = _artifactNamingValidatorService_;
      spyOn(artifactStore, 'load').and.returnValue(Promise.resolve([{id: 'person', type: 'page'}]));


      dataManagementRepo = _dataManagementRepo_;
      spyOn(dataManagementRepo, 'getDataObjects').and.returnValue($q.when({error: false, objects: []}));

      controller = $controller('EditorHeaderCtrl', {
        $window: $window,
        selectedResolution: {},
        $uibModal: $uibModal,
        $stateParams: $stateParams,
        $state: $state,
        artifactRepo: pageRepo,
        artifact: {},
        mode: 'page',
        $scope: scope,
        $localStorage: $localStorage,
        artifactStore: artifactStore,
        artifactNamingValidatorService: artifactNamingValidatorService,
        dataManagementRepo: dataManagementRepo
      });
    }));

    it('should navigate back', function () {
      controller.back();
      expect(browserHistoryService.back).toHaveBeenCalled();
      let fallback = browserHistoryService.back.calls.argsFor(0)[0];
      fallback();
      expect($state.go).toHaveBeenCalledWith('designer.home');
    });

    it('should save a page', function () {
      spyOn(pageRepo, 'save').and.returnValue($q.when({
        headers: () => {
        }
      }));
      spyOn(scope, '$broadcast').and.callThrough();
      var page = {id: 'person', name: 'person'};
      controller.dirty = true;

      controller.save(page);
      scope.$apply();

      expect(pageRepo.save).toHaveBeenCalledWith(page);
      expect(scope.$broadcast).toHaveBeenCalledWith('saved');
    });

    it('should not save a page if not dirty', function () {
      spyOn(pageRepo, 'save');
      spyOn(pageRepo, 'needSave').and.returnValue(false);
      var page = {id: 'person', name: 'person'};

      controller.save(page);
      scope.$apply();

      expect(pageRepo.save).not.toHaveBeenCalled();
    });

    it('should save a page changing its name', function () {
      let expectedHeaders = (headerName) => {
        let headers = {location: '/rest/pages/person'};
        return headers[headerName];
      };
      spyOn(pageRepo, 'save').and.returnValue($q.when({headers: expectedHeaders}));
      var page = {id: 'person', name: 'person', type: 'page'};
      controller.dirty = true;

      controller.save(page);
      scope.$apply();

      expect(pageRepo.save).toHaveBeenCalledWith(page);
      expect($state.go).toHaveBeenCalledWith('designer.page', $stateParams, {
        location: 'replace', reload: true
      });
    });

    it('should save and export page', function () {
      spyOn($uibModal, 'open').and.returnValue(modalInstance);
      spyOn(pageRepo, 'save').and.returnValue($q.when({
        headers: () => {
        }
      }));
      spyOn(pageRepo, 'exportUrl').and.returnValue('export/page/person');
      var page = {id: 'person'};

      controller.saveAndExport(page);

      expect($uibModal.open).toHaveBeenCalled();
      expect($uibModal.open.calls.mostRecent().args[0].templateUrl).toEqual('js/editor/header/export-popup.html');
      expect($uibModal.open.calls.mostRecent().args[0].controller).toEqual('ExportPopUpController');

      modalInstance.close({});
      scope.$apply();

      expect(pageRepo.save).toHaveBeenCalledWith(page);
      expect($window.location).toBe('export/page/person');
    });

    it('should save and export page without displaying message', function () {
      $localStorage.bonitaUIDesigner = {doNotShowExportMessageAgain: true};
      spyOn($uibModal, 'open').and.returnValue(modalInstance);
      spyOn(pageRepo, 'save').and.returnValue($q.when({
        headers: () => {
        }
      }));
      spyOn(pageRepo, 'exportUrl').and.returnValue('export/page/person');
      var page = {id: 'person'};

      controller.saveAndExport(page);
      scope.$apply();

      expect($uibModal.open).not.toHaveBeenCalled();

      expect(pageRepo.save).toHaveBeenCalledWith(page);
      expect($window.location).toBe('export/page/person');
    });

    it('should avoid save then export if page is not dirty', function () {
      $localStorage.bonitaUIDesigner = {doNotShowExportMessageAgain: true};
      spyOn($uibModal, 'open').and.returnValue(modalInstance);
      spyOn(pageRepo, 'save');
      spyOn(pageRepo, 'exportUrl').and.returnValue('export/page/person');
      var page = {id: 'person'};
      spyOn(pageRepo, 'needSave').and.returnValue(false);

      controller.saveAndExport(page);
      scope.$apply();

      expect($uibModal.open).not.toHaveBeenCalled();
      expect(pageRepo.save).not.toHaveBeenCalled();
      expect($window.location).toBe('export/page/person');
    });

    it('should save a page as ...', function () {
      spyOn($uibModal, 'open').and.returnValue(modalInstance);
      spyOn(pageRepo, 'create').and.returnValue($q.when({}));
      spyOn(controller, 'removeReferences').and.callThrough();
      var page = {id: 'person', type: 'page'};

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

    it('should update page metadata', function () {
      spyOn($uibModal, 'open').and.returnValue(modalInstance);
      spyOn(pageRepo, 'save').and.returnValue($q.when({
        headers: () => {
        }
      }));
      spyOn(pageRepo, 'loadResources').and.returnValue(['GET|living/application-menu']);

      var page = {id: 'person', type: 'page', displayName: 'display name', description: 'description'};

      controller.editMetadata(page);

      expect($uibModal.open).toHaveBeenCalled();
      expect($uibModal.open.calls.mostRecent().args[0].templateUrl).toEqual('js/editor/header/metadata-popup.html');
      expect($uibModal.open.calls.mostRecent().args[0].controller).toEqual('MetadataPopUpController');

      modalInstance.close(page);
      scope.$apply();

      expect(pageRepo.save).toHaveBeenCalledWith(page);
    });

    it('should remove reference attribute from every item', () => {
      expect(controller.removeReferences({})).toEqual({});
      expect(controller.removeReferences({reference: 'test'})).toEqual({});
      expect(controller.removeReferences([{reference: 'test'}])).toEqual([{}]);
      expect(controller.removeReferences({rows: [{reference: 'test'}]})).toEqual({rows: [{}]});
      let page = {
        'name': 'test3',
        'reference': 'bonita',
        'rows': [[{
          'type': 'fragment',
          'reference': '14bb674b-06ee-40e2-9639-432f0337937a',
          'id': '6c959a04-a8a8-4fde-b8d8-b76323cd1629'
        }]],
        'inactiveAssets': [], 'data': {}
      };
      page.rows[0].$$parentContainer = page;
      //using angular json conversion allow to remove properties starting with a $
      //avoiding a circular reference to page
      let result = angular.fromJson(angular.toJson(controller.removeReferences(page)));
      expect(result).toEqual({
        'name': 'test3',
        'rows': [[{'type': 'fragment', 'id': '6c959a04-a8a8-4fde-b8d8-b76323cd1629',}]],
        'inactiveAssets': [], 'data': {}
      });
    });

    it('should open convert popup', function () {
      spyOn($uibModal, 'open').and.returnValue(modalInstance);
      spyOn(pageRepo, 'save').and.returnValue($q.when({
        headers: () => {
        }
      }));
      var page = {id: 'person', type: 'page'};
      controller.convert(page);
      expect($uibModal.open).toHaveBeenCalled();
      expect($uibModal.open.calls.mostRecent().args[0].templateUrl).toEqual('js/editor/header/convert-popup.html');
      expect($uibModal.open.calls.mostRecent().args[0].controller).toEqual('ConvertPopUpController');
      modalInstance.close(page);
      scope.$apply();
      expect(pageRepo.save).toHaveBeenCalledWith(page);
    });

    it('should display business data model status when it\'s unable', function () {
      scope.$apply();
      expect(controller.businessDataRepositoryOffline).toEqual(false);
    });
  });
})();
