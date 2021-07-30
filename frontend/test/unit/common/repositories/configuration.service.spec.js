describe('configuration service', () => {
  let configuration, $httpBackend;

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories'));
  beforeEach(inject(function(_$httpBackend_, _configuration_) {
    $httpBackend = _$httpBackend_;
    configuration = _configuration_;
  }));

  it('should get the configuration', function() {
    // given a configuration info
    let configJson = {
      uidVersion: '1.13.0',
      modelVersion: '3.0',
      modelVersionLegacy: '2.2',
      bdrUrl: 'http://localhost:4000',
      experimentalMode: true
    };
    $httpBackend.whenGET('./rest/config').respond(200, configJson);

    // then we should have called the backend
    $httpBackend.flush();
    expect(configuration.getUidVersion()).toEqual('1.13.0');
    expect(configuration.getModelVersion()).toEqual('3.0');
    expect(configuration.getBdrUrl()).toEqual('http://localhost:4000');
    expect(configuration.isExperimentalModeEnabled()).toEqual(true);
  });

});

