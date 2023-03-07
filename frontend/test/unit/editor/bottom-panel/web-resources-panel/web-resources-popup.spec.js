(function () {
  'use strict';
  describe('webResourcePopupCtrl', function () {

    let $scope, controller, getController;

    beforeEach(angular.mock.module('bonitasoft.designer.webResources', 'mock.modal'));

    beforeEach(inject(function ($rootScope, $controller, $uibModalInstance) {
      $scope = $rootScope.$new();
      $uibModalInstance = $uibModalInstance.create();

      $scope.declareNewResource = {
        $setPristine: angular.noop
      };
      //Function inject as popup Parameter
      let isSameWebResourceSpy = function (a, b) {
        return a.verb.toLowerCase() === b.verb.toLowerCase() && a.value.toLowerCase() === b.value.toLowerCase();
      }

      getController = function (pageWebResources, data) {
        return $controller('WebResourcePopupCtrl', {
          $scope: $scope,
          $uibModalInstance: $uibModalInstance,
          webResources: pageWebResources,
          data: data,
          isSameWebResource: isSameWebResourceSpy
        });
      };
    }));

    it('should have a unique `value` when user declare a new web resource', function () {
      controller = getController(
        [
          {verb: 'get', value: 'identity/user', id: 0},
          {verb: 'get', value: 'bpm/userTask', id: 1}]
        ,
        {});
      expect(controller.isWebResourceUnique({verb: 'get', value: 'identity/user'})).toBeFalse();
      expect(controller.isWebResourceUnique({verb: 'post', value: 'identity/user'})).toBeTrue();
      expect(controller.isWebResourceUnique({verb: 'get', value: 'bpm/task'})).toBeTrue();
    });

    it('should have a unique `value` when user edit a web resource except himself', function () {
      controller = getController([
        {verb: 'get', value: 'identity/user', id: 0},
        {verb: 'get', value: 'bpm/userTask', id: 1}],
      {verb: 'get', value: 'identity/user', id: 0});
      expect(controller.isWebResourceUnique({verb: 'get', value: 'identity/user', id: 0})).toBeTrue();
      expect(controller.isWebResourceUnique({verb: 'post', value: 'identity/user', id: 0})).toBeTrue();
      expect(controller.isWebResourceUnique({verb: 'get', value: 'bpm/userTask', id: 0})).toBeFalse();
    });

    it('should return the label for http verb given on input', () => {
      controller = getController([], {});
      expect(controller.getVerbLabel('get')).toBe('GET');
      expect(controller.getVerbLabel('post')).toBe('POST');
      expect(controller.getVerbLabel('put')).toBe('PUT');
      expect(controller.getVerbLabel('delete')).toBe('DELETE');
    });
  });
})();
