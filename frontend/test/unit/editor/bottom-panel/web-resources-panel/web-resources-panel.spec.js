(function () {
  'use strict';
  describe('webResourceCtrl', function () {

    let $scope, $q, $uibModal, controller, getController, pageRepo;

    beforeEach(angular.mock.module('bonitasoft.designer.webResources',
      'mock.modal',
      'bonitasoft.designer.common.repositories',
      'bonitasoft.designer.editor.whiteboard'));

    beforeEach(inject(function ($rootScope, $controller, _$uibModal_, _$q_,_pageRepo_) {
      $scope = $rootScope.$new();

      $q = _$q_;
      $uibModal = _$uibModal_;
      $scope.declareNewResource = {
        $setPristine: angular.noop
      };
      pageRepo = _pageRepo_;

      spyOn($uibModal, 'open').and.returnValue({
        result: $q.when({})
      });

      getController = function (artifact) {
        return $controller('WebResourcesCtrl', {
          $scope: $scope,
          artifact: artifact,
          artifactRepo: pageRepo
        });
      };
    }));

    it('should be sort by default on value column ', function () {
      controller = getController({});
      expect(controller.$scope.sortCriteria).toBe('value')
    });

    it('should be filter webResources when user use the search filter', function () {
      spyOn(pageRepo, 'loadAutoWebResources').and.returnValue($q.when([{
        method: 'post',
        value: 'identity/professionalcontactdata',
        scopes: ['custom_startProcessBtn'],
        automatic: true
      },
      {method: 'post', value: 'identity/user', scopes: ['pbButton'], automatic: true}
      ]));

      controller = getController({
        webResources: [
          {method: 'get', value: 'bpm/userTask'},
          {method: 'get', value: 'bpm/userTask1'},
        ],
      });
      controller.$scope.$apply();

      expect(controller.getResources('bpm').length).toBe(2);
      expect(controller.getResources('user').length).toBe(3);
      expect(controller.getResources('xxx').length).toBe(0);
    });

    it('should be not updatable when web resource come from automatic detection', function () {
      controller = getController({});
      let resource = {
        method: 'post',
        value: 'identity/professionalcontactdata',
        scopes: ['custom_startProcessBtn'],
        automatic: true,
      };
      expect(controller.isAutomaticDetection(resource)).toBeTrue();
      expect(controller.isUpdatable(resource)).toBeFalse()

      resource = {
        method: 'post',
        value: 'identity/professionalcontactdata',
        scopes: ['custom_startProcessBtn']
      };
      expect(controller.isAutomaticDetection(resource)).toBeFalse();
      expect(controller.isUpdatable(resource)).toBeTrue()
    });

    it('should be updatable when web resource come from a manual detection', function () {
      controller = getController({});
      let resource = {
        method: 'post',
        value: 'identity/professionalcontactdata',
        scopes: ['custom_startProcessBtn']
      };
      expect(controller.isAutomaticDetection(resource)).toBeFalse();
      expect(controller.isUpdatable(resource)).toBeTrue()

      resource = {
        method: 'post',
        value: 'identity/professionalcontactdata',
        scopes: ['custom_startProcessBtn'],
        detection: 'manual'
      };
      expect(controller.isAutomaticDetection(resource)).toBeFalse();
      expect(controller.isUpdatable(resource)).toBeTrue()
    });

    it('should open the help popup when user click on button', function () {
      spyOn(pageRepo, 'loadAutoWebResources').and.returnValue($q.when([]));
      controller = getController({});
      controller.$scope.$apply();

      controller.openHelp();

      expect($uibModal.open).toHaveBeenCalled();
    });

    it('should add a new web resource when add popup is opened', function () {
      let resourceToAdd = {method: 'post', value: 'identity/user'};
      spyOn(pageRepo, 'loadAutoWebResources').and.returnValue($q.when([]));
      controller = getController({webResources: []});
      controller.$scope.$apply();

      controller.openWebResourcePopup();
      controller.addOrUpdateWebResource(resourceToAdd);

      expect($uibModal.open).toHaveBeenCalled();
      expect(controller.artifact.webResources.length).toBe(1);
    });

    it('should edit the web resource when edit popup is opened', function () {
      let resourceToAdd = {method: 'get', value: 'identity/user', id: 0};
      spyOn(pageRepo, 'loadAutoWebResources').and.returnValue($q.when([]));

      controller = getController({webResources: [{method: 'get', value: 'bpm/userTask', id: 0}]});
      controller.$scope.$apply();

      controller.openWebResourcePopup();
      controller.addOrUpdateWebResource(resourceToAdd);

      expect($uibModal.open).toHaveBeenCalled();
      expect(controller.artifact.webResources.length).toBe(1);
      expect(controller.artifact.webResources[0].value).toBe('identity/user');
    });

    it('should delete a web Resource', function () {
      spyOn(pageRepo, 'loadAutoWebResources').and.returnValue($q.when([]));

      controller = getController({
        webResources: [
          {method: 'get', value: 'bpm/userTask', id: 0},
          {method: 'post', value: 'bpm/manualTask', id: 1}
        ]
      });
      controller.$scope.$apply();

      const resourceToDelete = {method: 'get', value: 'bpm/userTask', id: 0};

      controller.openDeletePopup(resourceToDelete);
      controller.deleteResource(resourceToDelete);

      expect($uibModal.open).toHaveBeenCalled();
      expect(controller.artifact.webResources.length).toBe(1);
    });

    it('should return the same webResources when the web Resource to delete don\'t have an id', function () {
      spyOn(pageRepo, 'loadAutoWebResources').and.returnValue($q.when([]));
      controller = getController({
        webResources: [
          {method: 'get', value: 'bpm/userTask', id: 0},
          {method: 'post', value: 'bpm/manualTask', id: 1}
        ]
      });
      const resourceToDelete = {method: 'get', value: 'bpm/userTask'};

      controller.openDeletePopup(resourceToDelete);
      controller.deleteResource(resourceToDelete);
      controller.$scope.$apply();

      expect($uibModal.open).toHaveBeenCalled();
      expect(controller.artifact.webResources.length).toBe(2);
    });
  });
})();
