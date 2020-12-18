describe('artifactListController', function() {

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories', 'bonitasoft.designer.home', 'bonitasoft.designer.editor.whiteboard'));

  let $scope, $q, $uibModal, $localStorage, pageRepo, widgetRepo,element, migration;

  beforeEach(inject(function($rootScope, $compile, $injector) {

    $scope = $rootScope;
    $q = $injector.get('$q');
    $uibModal = $injector.get('$uibModal');
    $localStorage = $injector.get('$localStorage');
    $localStorage.bonitaUIDesigner = {};
    pageRepo = $injector.get('pageRepo');
    widgetRepo = $injector.get('widgetRepo');
    migration = $injector.get('migration');

    spyOn(pageRepo, 'migrate').and.returnValue($q.when({}));
    spyOn(pageRepo, 'migrationStatus').and.returnValue($q.when({}));
    spyOn(migration, 'handleMigrationStatus');
    spyOn(migration, 'handleMigrationNotif');

    $scope.refreshAll = jasmine.createSpy('refreshAll');
    $scope.downloadArtifact = jasmine.createSpy('downloadArtifact');
    $scope.artifacts = [
      {
        id: 'page1',
        type: 'page',
        name: 'Page 1',
        repo: pageRepo
      },
      {
        id: 'widget1',
        type: 'widget',
        name: 'Widget 1',
        repo: widgetRepo,
        custom: true
      }
    ];

    element = $compile('<artifact-list artifacts="artifacts" existing-artifacts="artifacts" refresh-all="refreshAll" download-artifact="downloadArtifact"></artifact-list>')($scope);
    $scope.$digest();
  }));

  it('should list all artifacts', function() {
    expect([].map.call(element.find('li .Artifact-name'), (artifact) => artifact.innerText.trim()))
      .toEqual(['Page 1', 'Widget 1']);
  });

  it('should open a confirmation dialog to confirm artifact deletion', function() {
    // given a fake modal service
    spyOn(pageRepo, 'delete').and.returnValue($q.when());
    var fakeModal = {
      result: $q.when('page1')
    };
    spyOn($uibModal, 'open').and.returnValue(fakeModal);

    element.find('#page1 .Artifact-delete').click();

    expect($uibModal.open).toHaveBeenCalled();

    // then the result callback should have been called
    $scope.$apply();

    expect(pageRepo.delete).toHaveBeenCalledWith('page1');
    expect($scope.refreshAll).toHaveBeenCalled();
  });

  it('should export an artifact', function() {
    spyOn(pageRepo, 'exportUrl').and.returnValue('export/page/page1');
    var fakeModal = {
      result: $q.when()
    };
    spyOn($uibModal, 'open').and.returnValue(fakeModal);

    element.find('#page1 .Artifact-export').click();

    expect($uibModal.open).toHaveBeenCalled();
    expect($uibModal.open.calls.mostRecent().args[0].templateUrl).toEqual('js/editor/header/export-popup.html');
    expect($uibModal.open.calls.mostRecent().args[0].controller).toEqual('ExportPopUpController');

    expect($scope.downloadArtifact).toHaveBeenCalledWith('export/page/page1');
  });

  it('should export an artifact without displaying message', function() {
    $localStorage.bonitaUIDesigner = { doNotShowExportMessageAgain: true };
    spyOn(pageRepo, 'exportUrl').and.returnValue('export/page/page1');
    var fakeModal = {
      result: $q.when()
    };
    spyOn($uibModal, 'open').and.returnValue(fakeModal);

    element.find('#page1 .Artifact-export').click();

    expect($uibModal.open).not.toHaveBeenCalled();

    expect($scope.downloadArtifact).toHaveBeenCalledWith('export/page/page1');
  });

  it('should rename an artifact if the name has changed', function() {
    var expectedHeaders = (headerName) => {
      var headers = {location : '/rest/pages/person'};
      return headers[headerName];
    };
    // given a page with a new name
    spyOn(pageRepo, 'rename').and.returnValue($q.when({headers : expectedHeaders}));

    // when renaming the page
    rename('#page1', 'test');

    // then it should have save the name and refresh the page
    expect(pageRepo.rename).toHaveBeenCalled();
    expect(element.find('#page1 input').controller('ngModel').$viewValue).toBe('test');
  });

  it('should revert the name if the rename of the artifact has failed', function() {
    spyOn(pageRepo, 'rename').and.returnValue($q.reject());

    rename('#page1', 'test');

    expect(pageRepo.rename).toHaveBeenCalled();
    expect(element.find('#page1 input').controller('ngModel').$viewValue).toBe('Page 1');
  });

  it('should not rename an artifact if the name has not changed', function() {
    // given a page with the same name as a new name
    spyOn(pageRepo, 'rename').and.returnValue($q.when());

    // when renaming the page
    rename('#page1', 'Page 1');

    // then it should not save and just toggle edition mode
    expect(pageRepo.rename).not.toHaveBeenCalled();
  });

  it('should show a message when there is no artifacts', function() {
    $scope.artifacts = [];
    $scope.$apply();

    expect(element.text().trim()).toBe('No artifact found.');
  });

  function rename(id, value) {
    element.find(`${id} .Artifact-rename`).click();
    element.find(`${id} input`).controller('ngModel').$setViewValue(value);
    element.find(`${id} input`).blur();
  }

});
