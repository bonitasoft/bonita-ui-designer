describe('Import artifact report controller', () => {
  var importArtifactService, importArtifactReportCtrl, report, modalInstance;

  beforeEach(angular.mock.module('bonitasoft.designer.home.import', 'mock.modal'));

  beforeEach(inject(function($controller, _importArtifactService_, _$modalInstance_) {
      importArtifactService = _importArtifactService_;
      modalInstance = _$modalInstance_.create();
      report = {};
      importArtifactReportCtrl = $controller('ImportReportPopupController', {
        $modalInstance: modalInstance,
        type: 'page',
        title: 'Import a new page',
        importReport: report
      });
    }));

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
