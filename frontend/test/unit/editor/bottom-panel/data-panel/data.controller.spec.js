(function() {
  'use strict';
  describe('DataCtrl', function() {

    var $scope, repository, $q, $location, $uibModal, gettextCatalog;

    beforeEach(angular.mock.module('bonitasoft.designer.editor.bottom-panel.data-panel'));

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

    it('should transform page data to an array', function () {
      $scope.page.data = {
        aKey: {type: 'constant', value: 'aValue'},
        jsonExample: {type: 'json', value: '{}'},
        urlExample: {type: 'url', value: 'https://api.github.com/users/jnizet'}
      };

      var variables = $scope.getVariables();

      expect(variables).toEqual([
        {type: 'constant', value: 'aValue'},
        {type: 'json', value: '{}'},
        {type: 'url', value: 'https://api.github.com/users/jnizet'}
      ]);
      // each object should have a property non enumerable named name
      expect(variables[0].name).toEqual('aKey');
      expect(variables[1].name).toEqual('jsonExample');
      expect(variables[2].name).toEqual('urlExample');
    });

    it('should filter variables by name', function () {
      $scope.page.data = {
        aKey: {type: 'constant', value: 'aValue'},
        jsonExample: {type: 'json', value: '{}'},
        urlExample: {type: 'url', value: 'https://api.github.com/users/jnizet'}
      };

      var filtered = $scope.getVariables('aKey');
      expect(filtered).toEqual([{type: 'constant', value: 'aValue'}]);

      filtered = $scope.getVariables('unknownkey');
      expect(filtered).toEqual([]);
    });

    it('should filter variables by value', function() {
      $scope.page.data = {
        aKey: {type: 'constant', value: 'aValue'},
        jsonExample: {type: 'json', value: '{}'},
        urlExample: {type: 'url', value: 'https://api.github.com/users/jnizet'}
      };

      var filtered = $scope.getVariables('aValue');
      expect(filtered).toEqual([{type: 'constant', value: 'aValue'}]);

      filtered = $scope.getVariables('unknownvalue');
      expect(filtered).toEqual([]);
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

    it('should sort based on a sort criteria', function() {

      $scope.sort('aCriteria');

      expect($scope.sortCriteria).toBe('aCriteria');
    });

    it('should change sort order while sorting twice on same criteria', function() {

      $scope.sort('aCriteria');
      expect($scope.isReversedSorting).toBe(false);

      $scope.sort('aCriteria');
      expect($scope.isReversedSorting).toBe(true);
    });

    it('should reset sort order while changing sort criteria', function() {
      $scope.isReversedSorting = true;
      $scope.sortCriteria = 'aCriteria';

      $scope.sort('anotherCriteria');

      expect($scope.isReversedSorting).toBe(false);
      expect($scope.sortCriteria).toBe('anotherCriteria');
    });

  });
})();
