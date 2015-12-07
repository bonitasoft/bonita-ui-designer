describe('import repository service', () => {

  var importRepo, $httpBackend;

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories'));

  beforeEach(inject(function(_importRepo_, _$httpBackend_) {
    importRepo = _importRepo_;
    $httpBackend = _$httpBackend_;
  }));

  it('should force an import based on a uuid', () => {
    var uuid = 'zezaerze-zerz-zer-zer';
    $httpBackend.expectPOST(`import/${uuid}/force`).respond({ 'an': 'importReport' });
    importRepo.forceImport(uuid)
      .then(response => expect(response).toEqual({ 'an': 'importReport' }));
    $httpBackend.flush();
  });

  it('should cancel an import based on a uuid', () => {
    var uuid = 'zezaerze-zerz-zer-zer';
    $httpBackend.expectPOST(`import/${uuid}/cancel`).respond({ 'an': 'importReport' });
    importRepo.cancelImport(uuid)
      .then(response => expect(response).toEqual({ 'an': 'importReport' }));
    $httpBackend.flush();
  });
});
