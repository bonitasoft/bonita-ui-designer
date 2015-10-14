describe('Import artifact', () => {
  var importArtifactService, alerts, scope, httpBackend, q, controller, modalInstance, deferredPageRepo, deferredWidgetRepo;
  beforeEach(angular.mock.module('bonitasoft.designer.home'));
  beforeEach(angular.mock.module('mock.modal'));
  beforeEach(inject(($injector, $rootScope, $httpBackend, $q, $controller, _$modalInstance_) => {
    scope = $rootScope;
    q = $q;
    alerts = $injector.get('alerts');
    httpBackend = $httpBackend;
    controller = $controller;
    importArtifactService = $injector.get('importArtifactService');
    deferredPageRepo = q.defer();
    deferredWidgetRepo = q.defer();
    modalInstance = _$modalInstance_.create();
    spyOn(importArtifactService.forceImportRepoFns, 'page').and.returnValue(deferredPageRepo.promise);
    spyOn(importArtifactService.forceImportRepoFns, 'widget').and.returnValue(deferredWidgetRepo.promise);
  }));
  describe('ImportArtifactService', () => {
    it('should check error message', () => {
      expect(importArtifactService.isErrorResponse()).toBeFalsy();
      expect(importArtifactService.isErrorResponse({})).toBeFalsy();
      expect(importArtifactService.isErrorResponse({
        type: ''
      })).toBeFalsy();
      expect(importArtifactService.isErrorResponse({
        message: 'an Error'
      })).toBeFalsy();
      expect(importArtifactService.isErrorResponse({
        type: 'error',
        message: 'an Error'
      })).toBeTruthy();
    });

    describe('manage import response', () => {
      it('should reject response processing when it is an error', function() {
        var message = 'error';
        var successFn = jasmine.createSpy('successFn');
        spyOn(importArtifactService, 'isErrorResponse').and.returnValue(true);
        spyOn(alerts, 'addError');

        importArtifactService.manageImportResponse('page', true, { message: message })
          .then(successFn, angular.noop);
        scope.$apply();
        
        expect(alerts.addError).toHaveBeenCalledWith(message);
        expect(successFn).not.toHaveBeenCalled();
      });
      it('should resolve response processing when override is false ', function() {
        var report = { overriden: false };
        var expectedReport = angular.extend(report, { type: 'page' });
        var errorFn = jasmine.createSpy('errorFn');
        spyOn(importArtifactService, 'isErrorResponse').and.returnValue(false);
        spyOn(alerts, 'addError');
        spyOn(alerts, 'addSuccess');

        importArtifactService.manageImportResponse('page', true, report).then((report) => expect(report).toEqual(expectedReport), errorFn);
        
        expect(alerts.addError).not.toHaveBeenCalled();
        expect(alerts.addSuccess).toHaveBeenCalledWith({
          title: 'Successful import',
          contentUrl: 'js/home/import-artifact-success-message.html',
          context: expectedReport
        }, 15000);
        expect(errorFn).not.toHaveBeenCalled();
      });
      it('should resolve response processing when checkOverride is false ', function() {
        var report = { overriden: true };
        var errorFn = jasmine.createSpy('errorFn');
        var expectedReport = angular.extend(report, { type: 'page' });
        spyOn(importArtifactService, 'isErrorResponse').and.returnValue(false);
        spyOn(alerts, 'addError');
        spyOn(alerts, 'addSuccess');

        importArtifactService.manageImportResponse('page', false, report).then((report) => expect(report).toBeUndefined(), errorFn);
        scope.$apply();
        
        expect(alerts.addError).not.toHaveBeenCalled();
        expect(alerts.addSuccess).toHaveBeenCalledWith({
          title: 'Successful import',
          contentUrl: 'js/home/import-artifact-success-message.html',
          context: expectedReport
        }, 15000);
        expect(errorFn).not.toHaveBeenCalled();
      });
      it('should resolve response processing when checkOverride is true and overriden is true ', function() {
        var report = { overridden: true };
        var errorFn = jasmine.createSpy('errorFn');
        var expectedReport = angular.extend(report, { type: 'page' });
        spyOn(importArtifactService, 'isErrorResponse').and.returnValue(false);
        spyOn(alerts, 'addError');
        spyOn(alerts, 'addSuccess');

        importArtifactService.manageImportResponse('page', true, report).then((report) => expect(report).toEqual(expectedReport), errorFn);
        scope.$apply();
        
        expect(alerts.addError).not.toHaveBeenCalled();
        expect(alerts.addSuccess).not.toHaveBeenCalled();
        expect(errorFn).not.toHaveBeenCalled();
      });
    });
    describe('force import', function() {
      it('should force import and call error callback on error', function() {
        var deferred = q.defer();
        var successFn = jasmine.createSpy('successFn');
        var errorFn = jasmine.createSpy('errorFn');
        var report = {};
        spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);

        importArtifactService.forceImport({ uuid: 'tmpFileName.zip' }, 'page', successFn, errorFn);
        deferred.reject();
        deferredPageRepo.resolve({data:report});
        scope.$apply();
        
        expect(importArtifactService.manageImportResponse).toHaveBeenCalledWith('page', false, report);
        expect(successFn).not.toHaveBeenCalled();
        expect(errorFn).toHaveBeenCalled();
      });

      it('should force import and call succes callback', function() {
        var deferred = q.defer();
        var successFn = jasmine.createSpy('successFn');
        var errorFn = jasmine.createSpy('errorFn');
        var report = {};
        spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);

        importArtifactService.forceImport({
          uuid: 'tmpFileName.zip'
        }, 'widget', successFn, errorFn);
        deferred.resolve();
        deferredWidgetRepo.resolve({data: report});
        scope.$apply();
        
        expect(importArtifactService.manageImportResponse).toHaveBeenCalledWith('widget', false, report);
        expect(successFn).toHaveBeenCalled();
        expect(errorFn).not.toHaveBeenCalled();
      });
    });
  });
  describe('ImportArtifactController', () => {
    var importArtifactCtrl;
    beforeEach(function() {
      importArtifactCtrl = controller('ImportArtifactController', {
        $modalInstance: modalInstance,
        type: 'page',
        title: 'Import a new page'
      });
    });
    it('should expose data for view', () => {
      expect(importArtifactCtrl.type).toEqual('page');
      expect(importArtifactCtrl.url).toEqual('import/page');
      expect(importArtifactCtrl.filename).toEqual('');
      expect(importArtifactCtrl.popupTitle).toEqual('Import a new page');
    });
    it('should manage importReport and close modal when upload is complete', () => {
      var deferred = q.defer();
      var importReport = {};
      spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);

      importArtifactCtrl.onComplete(importReport);
      deferred.resolve();
      scope.$apply();

      expect(importArtifactService.manageImportResponse).toHaveBeenCalledWith('page', true, importReport);
      expect(modalInstance.close).toHaveBeenCalled();
    });
    it('should manage importReport and dismiss modal when upload is complete and import management failed', () => {
      var deferred = q.defer();
      var importReport = {};
      spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);

      importArtifactCtrl.onComplete(importReport);
      deferred.reject();
      scope.$apply();

      expect(importArtifactService.manageImportResponse).toHaveBeenCalledWith('page', true, importReport);
      expect(modalInstance.dismiss).toHaveBeenCalled();
    });
  });
  describe('ImportArtifactReportController', () => {
    var importArtifactReportCtrl, report;
    beforeEach(function() {
      report = {};
      importArtifactReportCtrl = controller('ImportArtifactReportController', {
        $modalInstance: modalInstance,
        type: 'page',
        title: 'Import a new page',
        importReport: report
      });
    });
    it('should expose data for view', () => {
      expect(importArtifactReportCtrl.report).toEqual(report);
      expect(importArtifactReportCtrl.popupTitle).toEqual('Import a new page');
    });
    it('should force import', () => {
      spyOn(importArtifactService, 'forceImport');
      importArtifactReportCtrl.forceImport();
      expect(importArtifactService.forceImport).toHaveBeenCalledWith(report, 'page', modalInstance.close, modalInstance.dismiss);
    });
  });
});
