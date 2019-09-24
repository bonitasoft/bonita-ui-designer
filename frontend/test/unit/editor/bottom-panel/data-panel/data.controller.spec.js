(function() {
  'use strict';
  describe('DataCtrl', function() {

    var $scope, repository, $q, $location, $uibModal, gettextCatalog;

    beforeEach(angular.mock.module('bonitasoft.designer.editor.bottom-panel.data-panel', 'bonitasoft.designer.editor.whiteboard'));

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
      $scope.page.variables = {};
      var data = {
        $$name: 'colin',
        displayValue: 4,
        exposed: false,
        type: 'constant'
      };
      $scope.save(data);
      $scope.$apply();

      expect(data).toEqual(jasmine.objectContaining($scope.page.variables.colin));
    });

    it('should edit a data with default value', function() {
      $scope.exposableData = true ;
      $scope.page.variables = {
        $$name: 'myDataToEdit',
        displayValue: 'defaultValue',
        exposed: false,
        type: 'constant'
      };
      let dataToSave = {
        $$name: 'myDataToEdit',
        displayValue: 'defaultValue',
        exposed: true,
        type: 'constant'
      };
      let expectedDataSaved = {
        $$name: 'myDataToEdit',
        displayValue: '',
        exposed: true,
        type: 'constant'
      };
      $scope.save(dataToSave);
      $scope.$apply();

      expect(expectedDataSaved).toEqual(jasmine.objectContaining($scope.page.variables.myDataToEdit));
    });

    it('should delete a data', function() {
      $scope.page.variables = { name: 'colin', displayValue: 4 };

      $scope.delete('colin');
      $scope.$apply();
      expect($scope.page.variables.hasOwnProperty('colin')).toBe(false);
    });

    it('should transform page data to an array', function () {
      $scope.page.variables = {
        aKey: {type: 'constant', displayValue: 'aValue'},
        jsonExample: {type: 'json', displayValue: '{}'},
        urlExample: {type: 'url', displayValue: 'https://api.github.com/users/jnizet'}
      };

      var variables = $scope.getVariables();

      expect(variables).toEqual([
        {type: 'constant', displayValue: 'aValue'},
        {type: 'json', displayValue: '{}'},
        {type: 'url', displayValue: 'https://api.github.com/users/jnizet'}
      ]);
      // each object should have a property non enumerable named name
      expect(variables[0].name).toEqual('aKey');
      expect(variables[1].name).toEqual('jsonExample');
      expect(variables[2].name).toEqual('urlExample');
    });

    it('should filter variables by name', function () {
      $scope.page.variables = {
        aKey: {type: 'constant', displayValue: 'aValue'},
        jsonExample: {type: 'json', displayValue: '{}'},
        urlExample: {type: 'url', displayValue: 'https://api.github.com/users/jnizet'}
      };

      var filtered = $scope.getVariables('aKey');
      expect(filtered).toEqual([{type: 'constant', displayValue: 'aValue'}]);

      filtered = $scope.getVariables('unknownkey');
      expect(filtered).toEqual([]);
    });

    it('should filter variables by value', function() {
      $scope.page.variables = {
        aKey: {type: 'constant', displayValue: 'aValue'},
        jsonExample: {type: 'json', displayValue: '{}'},
        urlExample: {type: 'url', displayValue: 'https://api.github.com/users/jnizet'}
      };

      var filtered = $scope.getVariables('aValue');
      expect(filtered).toEqual([{type: 'constant', displayValue: 'aValue'}]);

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
        displayValue: 4,
        exposed: false,
        type: 'constant'
      };

      expect($scope.getType(data)).toEqual('String');
    });

    it('should return (Exposed) for exposed data type', function() {
      var data = {
        $$name: 'colin',
        displayValue: 4,
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

    it('should be sorted by name by default', function() {
      expect($scope.sortCriteria).toBe('name');
    });

  });
})();
