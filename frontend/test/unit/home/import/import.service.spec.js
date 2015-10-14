describe('Import service', () => {

  var importArtifactService, alerts, scope;

  beforeEach(angular.mock.module('bonitasoft.designer.home.import', 'mock.modal'));

  beforeEach(inject((_importArtifactService_, _alerts_, $rootScope) => {
    scope = $rootScope;
    alerts = _alerts_;
    importArtifactService = _importArtifactService_;
  }));

  it('should check error message', () => {
    expect(importArtifactService.isErrorResponse()).toBeFalsy();
    expect(importArtifactService.isErrorResponse({})).toBeFalsy();
    expect(importArtifactService.isErrorResponse({type: ''})).toBeFalsy();
    expect(importArtifactService.isErrorResponse({message: 'an Error'})).toBeFalsy();
    expect(importArtifactService.isErrorResponse({type: 'error', message: 'an Error'})).toBeTruthy();
  });

  describe('when managing import response', () => {

    it('should reject response processing when it is an error', function () {
      var message = 'error';
      var successFn = jasmine.createSpy('successFn');
      spyOn(importArtifactService, 'isErrorResponse').and.returnValue(true);
      spyOn(alerts, 'addError');

      importArtifactService.manageImportResponse('page', true, {message: message}).then(successFn, angular.noop);
      scope.$apply();

      expect(alerts.addError).toHaveBeenCalledWith(message);
      expect(successFn).not.toHaveBeenCalled();
    });

    it('should resolve response processing when override is false ', function () {
      var report = {overriden: false};
      var expectedReport = angular.extend(report, {type: 'page'});
      var errorFn = jasmine.createSpy('errorFn');
      spyOn(importArtifactService, 'isErrorResponse').and.returnValue(false);
      spyOn(alerts, 'addError');
      spyOn(alerts, 'addSuccess');

      importArtifactService.manageImportResponse('page', true, report).then((report) => expect(report).toEqual(expectedReport), errorFn);

      expect(alerts.addError).not.toHaveBeenCalled();
      expect(alerts.addSuccess).toHaveBeenCalledWith({
        title: 'Successful import',
        contentUrl: 'js/home/import/import-success-message.html',
        context: expectedReport
      }, 15000);
      expect(errorFn).not.toHaveBeenCalled();
    });

    it('should resolve response processing when checkOverride is false ', function () {
      var report = {overriden: true};
      var errorFn = jasmine.createSpy('errorFn');
      var expectedReport = angular.extend(report, {type: 'page'});
      spyOn(importArtifactService, 'isErrorResponse').and.returnValue(false);
      spyOn(alerts, 'addError');
      spyOn(alerts, 'addSuccess');

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

    it('should resolve response processing when checkOverride is true and overriden is true ', function () {
      var report = {overridden: true};
      var errorFn = jasmine.createSpy('errorFn');
      var expectedReport = angular.extend(report, {type: 'page'});
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

  describe('when forcing import', function () {
    var deferredPageRepo, deferredWidgetRepo, $q;

    beforeEach(inject((_$q_) => {
      $q = _$q_;
      deferredPageRepo = $q.defer();
      deferredWidgetRepo = $q.defer();
      spyOn(importArtifactService.forceImportRepoFns, 'page').and.returnValue(deferredPageRepo.promise);
      spyOn(importArtifactService.forceImportRepoFns, 'widget').and.returnValue(deferredWidgetRepo.promise);
    }));

    it('should force import and call error callback on error', function () {
      var deferred = $q.defer();
      var successFn = jasmine.createSpy('successFn');
      var errorFn = jasmine.createSpy('errorFn');
      var report = {};
      spyOn(importArtifactService, 'manageImportResponse').and.returnValue(deferred.promise);

      importArtifactService.forceImport({uuid: 'tmpFileName.zip'}, 'page', successFn, errorFn);
      deferred.reject();
      deferredPageRepo.resolve({data: report});
      scope.$apply();

      expect(importArtifactService.manageImportResponse).toHaveBeenCalledWith('page', false, report);
      expect(successFn).not.toHaveBeenCalled();
      expect(errorFn).toHaveBeenCalled();
    });

    it('should force import and call succes callback', function () {
      var deferred = $q.defer();
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
