describe('Import service', () => {

  var importArtifactService, importErrorMessagesService, alerts, scope, successFn, errorFn;

  beforeEach(angular.mock.module('bonitasoft.designer.home.import', 'mock.modal'));

  beforeEach(inject((_importArtifactService_, _alerts_, $rootScope, _importErrorMessagesService_) => {
    scope = $rootScope;
    alerts = _alerts_;
    importArtifactService = _importArtifactService_;
    importErrorMessagesService = _importErrorMessagesService_;

    successFn = jasmine.createSpy('successFn');
    errorFn = jasmine.createSpy('errorFn');
  }));

  it('should check error message', () => {
    expect(importArtifactService.isErrorResponse()).toBeFalsy();
    expect(importArtifactService.isErrorResponse({})).toBeFalsy();
    expect(importArtifactService.isErrorResponse({ type: '' })).toBeFalsy();
    expect(importArtifactService.isErrorResponse({ message: 'an Error' })).toBeFalsy();
    expect(importArtifactService.isErrorResponse({ type: 'error', message: 'an Error' })).toBeTruthy();
  });

  describe('when managing import response', () => {

    beforeEach(function() {
      spyOn(alerts, 'addError');
      spyOn(alerts, 'addSuccess');
    });

    it('should reject response processing when it is an error', function() {
      spyOn(importArtifactService, 'isErrorResponse').and.returnValue(true);
      var message = 'error';

      importArtifactService.manageImportResponse('page', true, { message: message }).then(successFn, angular.noop);
      scope.$apply();

      expect(alerts.addError).toHaveBeenCalled();
      expect(successFn).not.toHaveBeenCalled();
    });

    it('should get message from error message service when it is an error', function() {
      spyOn(importArtifactService, 'isErrorResponse').and.returnValue(true);
      var errorToDisplay = { cause: 'a cause', consequence: 'a consequence', additionalInfos: 'some aditional infos' };
      spyOn(importErrorMessagesService, 'getErrorContext').and.returnValue(errorToDisplay);

      importArtifactService.manageImportResponse('page', true, { message: 'Message from backend' }).then(successFn, angular.noop);
      scope.$apply();

      expect(alerts.addError).toHaveBeenCalledWith({
        title: 'Import error',
        contentUrl: 'js/home/import/import-error-message.html',
        context: errorToDisplay });
    });

    it('should resolve response processing when override is false ', function() {
      var report = { overriden: false };
      var expectedReport = angular.extend(report, { type: 'page' });
      spyOn(importArtifactService, 'isErrorResponse').and.returnValue(false);

      importArtifactService.manageImportResponse('page', true, report).then((report) => expect(report).toEqual(expectedReport), errorFn);

      expect(alerts.addError).not.toHaveBeenCalled();
      expect(alerts.addSuccess).toHaveBeenCalledWith({
        title: 'Successful import',
        contentUrl: 'js/home/import/import-success-message.html',
        context: expectedReport
      }, 15000);
      expect(errorFn).not.toHaveBeenCalled();
    });

    it('should resolve response processing when checkOverride is false ', function() {
      var report = { overriden: true };
      var expectedReport = angular.extend(report, { type: 'page' });
      spyOn(importArtifactService, 'isErrorResponse').and.returnValue(false);

      importArtifactService.manageImportResponse('page', false, report).then((report) => expect(report).toBeUndefined(), errorFn);
      scope.$apply();

      expect(alerts.addError).not.toHaveBeenCalled();
      expect(alerts.addSuccess).toHaveBeenCalledWith({
        title: 'Successful import',
        contentUrl: 'js/home/import/import-success-message.html',
        context: expectedReport
      }, 15000);
      expect(errorFn).not.toHaveBeenCalled();
    });

    it('should resolve response processing when checkOverride is true and overriden is true ', function() {
      var report = { overridden: true };
      var expectedReport = angular.extend(report, { type: 'page' });
      spyOn(importArtifactService, 'isErrorResponse').and.returnValue(false);

      importArtifactService.manageImportResponse('page', true, report).then((report) => expect(report).toEqual(expectedReport), errorFn);
      scope.$apply();

      expect(alerts.addError).not.toHaveBeenCalled();
      expect(alerts.addSuccess).not.toHaveBeenCalled();
      expect(errorFn).not.toHaveBeenCalled();
    });
  });

  describe('when forcing import', function() {
    var deferredPageRepo, deferredWidgetRepo, $q;

    beforeEach(inject((_$q_, widgetRepo, pageRepo) => {
      $q = _$q_;
      deferredPageRepo = $q.defer();
      deferredWidgetRepo = $q.defer();
      spyOn(pageRepo, 'forceImport').and.returnValue(deferredPageRepo.promise);
      spyOn(widgetRepo,'forceImport').and.returnValue(deferredWidgetRepo.promise);
    }));

    it('should force import and call error callback on error', function() {
      var deferred = $q.defer();
      var report = {};
      spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);

      importArtifactService.forceImport({ uuid: 'tmpFileName.zip' }, 'page', successFn, errorFn);
      deferred.reject();
      deferredPageRepo.resolve({ data: report });
      scope.$apply();

      expect(importArtifactService.manageImportResponse).toHaveBeenCalledWith('page', false, report);
      expect(successFn).not.toHaveBeenCalled();
      expect(errorFn).toHaveBeenCalled();
    });

    it('should force import and call succes callback', function() {
      var deferred = $q.defer();
      var report = {};
      spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);

      importArtifactService.forceImport({
        uuid: 'tmpFileName.zip'
      }, 'widget', successFn, errorFn);
      deferred.resolve();
      deferredWidgetRepo.resolve({ data: report });
      scope.$apply();

      expect(importArtifactService.manageImportResponse).toHaveBeenCalledWith('widget', false, report);
      expect(successFn).toHaveBeenCalled();
      expect(errorFn).not.toHaveBeenCalled();
    });
  });
});
