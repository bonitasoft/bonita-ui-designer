(function() {
  'use strict';
  describe('DataCtrl', function() {

    var $scope, repository, $q, $location, $uibModal, gettextCatalog;

    beforeEach(angular.mock.module('bonitasoft.designer.editor.data-panel'));

    beforeEach(inject(function($rootScope, $controller, _$location_, _pageRepo_, _$q_, _$uibModal_, _gettextCatalog_) {
      $scope = $rootScope.$new();

      $scope.addData = {
        $setPristine: angular.noop
      };

      $location = _$location_;
      repository = _pageRepo_;
      gettextCatalog = _gettextCatalog_;
      $q = _$q_;
      $uibModal = _$uibModal_;

      $controller('DataCtrl', {
        $scope: $scope,
        artifactRepo: repository,
        mode: 'page',
        artifact: {}
      });

    }));

    it('should save a new data', function() {
      $scope.page.data = {};
      var data = {
        $$name: 'colin',
        value: 4,
        exposed: false,
        type: 'constant'
      };
      $scope.save(data);
      $scope.$apply();

      expect(data).toEqual(jasmine.objectContaining($scope.page.data.colin));
    });

    it('should delete a data', function() {
      $scope.page.data = { name: 'colin', value: 4 };

      $scope.delete('colin');
      $scope.$apply();
      expect($scope.page.data.hasOwnProperty('colin')).toBe(false);
    });

    it('should filter a data that match a key value', function() {
      var data = {
        'colin': { value: 4 },
        'api': { value: 'user' }
      };

      $scope.page.data = data;
      $scope.searchedData = 'colin';
      $scope.$apply();

      expect($scope.pageData).toEqual({ 'colin': { value: 4 } });
    });

    it('should filter a data that match a data value', function() {
      var data = {
        'colin': { value: 4 },
        'api': { value: { 'user': 'toto' } }
      };

      $scope.page.data = data;
      $scope.searchedData = 'use';
      $scope.$apply();

      expect($scope.pageData).toEqual({ 'api': { value: { 'user': 'toto' } } });
    });

    it('should reset filter for data', function() {
      var data = {
        'colin': { value: 4 },
        'api': { value: 'user' }
      };

      $scope.page.data = data;
      $scope.searchedData = 'colin';
      $scope.$apply();

      expect($scope.pageData).toEqual({ 'colin': { value: 4 } });
      $scope.searchedData = '';
      $scope.$apply();
      expect($scope.pageData).toEqual(data);
    });

    it('should open a data popup', function() {
      spyOn($uibModal, 'open').and.returnValue({
        result: $q.when({})
      });
      $scope.openDataPopup();
      expect($uibModal.open).toHaveBeenCalled();
    });

    it('should return none exposed data type', function() {
      var data = {
        $$name: 'colin',
        value: 4,
        exposed: false,
        type: 'constant'
      };

      expect($scope.getType(data)).toEqual('String');
    });

    it('should return (Exposed) for exposed data type', function() {
      var data = {
        $$name: 'colin',
        value: 4,
        exposed: true,
        type: 'constant'
      };
      $scope.exposableData = true ;

      expect($scope.getType(data)).toEqual('(Exposed)');
    });

  });
})();
