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
      var report = {
        overridden: false,
        element: { type: 'page' }
      };
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

    // TODO to be removed when widget has type
    it('should set import report context type to \'widget\' when import report element has no type', function() {
      var report = {
        overridden: false,
        element: {  }
      };
      var expectedReport = angular.extend(report, { type: 'widget' });
      spyOn(importArtifactService, 'isErrorResponse').and.returnValue(false);

      importArtifactService.manageImportResponse('widget', true, report).then((report) => expect(report).toEqual(expectedReport), errorFn);

    });

    it('should resolve response processing when checkOverride is false ', function() {
      var report = {
        overridden: false,
        element: { type: 'page' }
      };
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
      var report = { overridden: true,
          element: { type: 'page' }
      };
      var expectedReport = angular.extend(report, { type: 'page' });
      spyOn(importArtifactService, 'isErrorResponse').and.returnValue(false);

      importArtifactService.manageImportResponse('page', true, report).then((report) => expect(report).toEqual(expectedReport), errorFn);
      scope.$apply();

      expect(alerts.addError).not.toHaveBeenCalled();
      expect(alerts.addSuccess).not.toHaveBeenCalled();
      expect(errorFn).not.toHaveBeenCalled();
    });
  });

});
