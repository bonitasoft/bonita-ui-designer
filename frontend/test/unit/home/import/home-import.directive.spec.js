describe('home import', () => {

  var element, $scope, controller, importArtifactService, q;

  beforeEach(angular.mock.module('bonitasoft.designer.home', 'mock.modal'));

  beforeEach(inject(function($compile, $rootScope, $q, _importArtifactService_) {
    $scope = $rootScope.$new();
    q = $q;
    importArtifactService = _importArtifactService_;
    $scope.refreshAll = jasmine.createSpy('refreshAll');
    $scope.filters = {};
    element = $compile('<uid-import-artifact></uid-import-artifact>')($scope);
    $scope.$apply();
    controller = element.controller('uidImportArtifact');
  }));

  describe('controller', () => {

    var $modalInstance, $modal;

    beforeEach(inject(function(_$modalInstance_, _$modal_) {
      $modalInstance = _$modalInstance_;
      $modal = _$modal_;
    }));

    it('should not open a modal when no report is returned', function() {
      var deferred = q.defer();
      spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);


      controller.onComplete({});
      deferred.resolve();
      $scope.$apply();

      expect($scope.refreshAll).toHaveBeenCalled();
    });

    it('should open a modal when import report is returned', function() {
      var importReport = {}, forceImportModal = $modalInstance.fake(), deferred = q.defer();

      spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);
      spyOn($modal, 'open').and.returnValue(forceImportModal);
      deferred.resolve(importReport);

      controller.onComplete(importReport);
      $scope.$apply();

      expect($modal.open.calls.count()).toEqual(1);
      var [args] = $modal.open.calls.mostRecent().args;
      expect(args.templateUrl).toEqual('js/home/import/import-report-popup.html');
      expect(args.resolve.importReport()).toEqual(importReport);
    });

    it('should refresh lists when second pop up is closed', function() {
      var importReport = {}, forceImportModal = $modalInstance.fake(), deferred = q.defer();

      spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);
      spyOn($modal, 'open').and.returnValue(forceImportModal);
      deferred.resolve(importReport);

      controller.onComplete(importReport);

      forceImportModal.close();
      $scope.$apply();

      expect($scope.refreshAll).toHaveBeenCalled();
    });

    it('should not refresh lists when second pop up is canceled', function() {
      var importReport = {}, forceImportModal = $modalInstance.fake(), deferred = q.defer();

      spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);

      spyOn($modal, 'open').and.returnValue(forceImportModal);
      deferred.resolve(importReport);

      controller.onComplete(importReport);

      forceImportModal.dismiss();
      $scope.$apply();

      expect($scope.refreshAll).not.toHaveBeenCalled();
    });
    it('should expose data for view', () => {
      expect(controller.type).toEqual('artifact');
      expect(controller.url).toEqual('import/artifact');
      expect(controller.filename).toEqual('');
      expect(controller.popupTitle).toEqual('Import a UI Designer artifact');
    });

    it('should manage importReport and close modal when upload is complete', () => {
      var deferred = q.defer();
      var importReport = {};
      spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);

      controller.onComplete(importReport);
      deferred.resolve();
      $scope.$apply();

      expect(importArtifactService.manageImportResponse).toHaveBeenCalledWith('artifact', true, importReport);
      expect(controller.isOpen).toBeFalsy();
    });

    it('should manage importReport and dismiss modal when upload is complete and import management failed', () => {
      var deferred = q.defer();
      var importReport = {};
      spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);

      controller.onComplete(importReport);
      deferred.reject();
      $scope.$apply();

      expect(importArtifactService.manageImportResponse).toHaveBeenCalledWith('artifact', true, importReport);
      expect(controller.isOpen).toBeFalsy();
    });
  });

});
