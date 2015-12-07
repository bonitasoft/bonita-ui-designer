describe('home import', () => {

  var element, $scope, controller;

  beforeEach(angular.mock.module('bonitasoft.designer.home', 'mock.modal'));

  beforeEach(inject(function($compile, $rootScope) {
    $scope = $rootScope.$new();
    $scope.refreshAll = jasmine.createSpy('refreshAll');
    $scope.filters = {};
    element = $compile('<uid-import-artifact></uid-import-artifact>')($scope);
    $scope.$apply();
    controller = element.controller('uidImportArtifact');
  }));

  describe('directive', () => {
    it('should import an artifact', function() {
      spyOn(controller, 'importElement');

      element.find('.HomeImport').click();

      expect(controller.importElement).toHaveBeenCalled();
    });
  });


  describe('controller', () => {

    var $modalInstance, $modal;

    beforeEach(inject(function(_$modalInstance_, _$modal_) {
      $modalInstance = _$modalInstance_;
      $modal= _$modal_;
    }));

    it('should open modal to create an artifact', () => {
      spyOn($modal, 'open').and.returnValue($modalInstance.create());

      controller.createElement();

      var [[args]] = $modal.open.calls.allArgs();
      expect(args.templateUrl).toEqual('js/home/create-popup.html');
      expect(args.controller).toEqual('CreatePopupController');
    });

    it('should open modal to import an artifact', () => {
      spyOn($modal, 'open').and.returnValue($modalInstance.create());

      controller.importElement();

      var [[args]] = $modal.open.calls.allArgs();
      expect(args.templateUrl).toEqual('js/home/import/import-popup.html');
      expect(args.resolve.type()).toEqual('artifact');
      expect(args.resolve.title()).toEqual('Import a UI Designer artifact');
    });

    it('should not open a second modal when no report is returned', function() {
      var importReport = null, modalInstance = $modalInstance.fake();
      spyOn($modal, 'open').and.returnValue(modalInstance);

      controller.importElement();
      modalInstance.close(importReport);
      $scope.$apply();

      expect($modal.open.calls.count()).toEqual(1);
      expect($scope.refreshAll).toHaveBeenCalled();
    });

    it('should open a second modal when import report is returned', function() {
      var importReport = {}, modalInstance = $modalInstance.fake();
      spyOn($modal, 'open').and.returnValue(modalInstance);

      controller.importElement();
      modalInstance.close(importReport);
      $scope.$apply();

      expect($modal.open.calls.count()).toEqual(2);
      var [args] = $modal.open.calls.mostRecent().args;
      expect(args.templateUrl).toEqual('js/home/import/import-report-popup.html');
      expect(args.resolve.importReport()).toEqual(importReport);
    });

    it('should refresh lists when second pop up is closed', function() {
      var importReport = {}, importModal = $modalInstance.fake(), forceImportModal = $modalInstance.fake();
      spyOn($modal, 'open').and.returnValue(importModal).and.returnValue(forceImportModal);
      controller.importElement();
      importModal.close(importReport);
      $scope.$apply();

      forceImportModal.close();
      $scope.$apply();

      expect($scope.refreshAll).toHaveBeenCalled();
    });

    it('should not refresh lists when second pop up is canceled', function() {
      var importReport = {}, importModal = $modalInstance.fake(), forceImportModal = $modalInstance.fake();
      spyOn($modal, 'open').and.returnValue(importModal).and.returnValue(forceImportModal);
      controller.importElement();
      importModal.close(importReport);
      $scope.$apply();

      forceImportModal.dismiss();
      $scope.$apply();

      expect($scope.refreshAll).not.toHaveBeenCalled();
    });
  });

});
