describe('Import popup controller', () => {

  var importArtifactService, importArtifactCtrl, scope, q, modalInstance;

  beforeEach(angular.mock.module('bonitasoft.designer.home.import', 'mock.modal'));

  beforeEach(inject(function(_$modalInstance_, $controller, _$q_, _importArtifactService_, $rootScope) {
    scope = $rootScope.$new();
    importArtifactService = _importArtifactService_;
    modalInstance = _$modalInstance_.create();
    q = _$q_;

    importArtifactCtrl = $controller('ImportPopupController', {
      $modalInstance: modalInstance,
      type: 'page',
      title: 'Import a new page'
    });
  }));

  it('should expose data for view', () => {
    expect(importArtifactCtrl.type).toEqual('page');
    expect(importArtifactCtrl.url).toEqual('import/artifact');
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
