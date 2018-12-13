describe('artifact naming validator service', function() {
  var artifactNamingValidatorService;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.common'));
  beforeEach(inject(function(_artifactNamingValidatorService_) {
    artifactNamingValidatorService = _artifactNamingValidatorService_;
  }));

  it('should return false when new artifact name don\'t exist', function() {
    let artifactList = [{'type':'page','name':'myOldPage'},{'type':'page','name':'myOldPage2'}];
    let isAlreadyExist = artifactNamingValidatorService.isArtifactNameAlreadyUseForType('myNewPage', 'page',artifactList);

    expect(isAlreadyExist).toBe(false);
  });

  it('should return true when new artifact name already exist', function() {
    let artifactList = [{'type':'page','name':'mynewpage'},{'type':'page','name':'myOldPage2'}];
    let isAlreadyExist = artifactNamingValidatorService.isArtifactNameAlreadyUseForType('myNewPage', 'page',artifactList);

    expect(isAlreadyExist).toBe(true);
  });
});
