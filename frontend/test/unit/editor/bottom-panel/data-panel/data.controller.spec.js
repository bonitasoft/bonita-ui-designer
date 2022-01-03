(function() {
  'use strict';
  describe('DataCtrl', function() {

    let $scope, repository, $q, $uibModal, getController;

    beforeEach(angular.mock.module('bonitasoft.designer.editor.bottom-panel.data-panel', 'bonitasoft.designer.editor.whiteboard'));

    beforeEach(inject(function($rootScope, $controller, _pageRepo_, _$q_, _$uibModal_) {
      $scope = $rootScope.$new();

      $scope.addData = {
        $setPristine: angular.noop
      };

      repository = _pageRepo_;
      $q = _$q_;
      $uibModal = _$uibModal_;

      getController = function(isAction) {
        return $controller('DataCtrl', {
          $scope: $scope,
          artifactRepo: repository,
          mode: 'page',
          isAction: isAction,
          artifact: {}
        });
      };


    }));

    it('should save a new data', function() {
      getController(false);
      $scope.page.variables = {};
      let data = {
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
      getController(false);
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
      getController(false);
      $scope.page.variables = { name: 'colin', displayValue: 4 };

      $scope.delete('colin');
      $scope.$apply();
      expect($scope.page.variables.hasOwnProperty('colin')).toBe(false);
    });

    it('should transform page data to an array', function () {
      // Variables
      getController(false);
      $scope.page.variables = {
        aKey: {type: 'constant', displayValue: 'aValue'},
        jsonExample: {type: 'json', displayValue: '{}'},
        urlExample: {type: 'url', displayValue: 'https://api.github.com/users/jnizet'}
      };

      let variables = $scope.getVariables();
      expect(variables).toEqual([
        {type: 'constant', displayValue: 'aValue'},
        {type: 'json', displayValue: '{}'},
      ]);
      // each object should have a property non enumerable named name
      expect(variables[0].name).toEqual('aKey');
      expect(variables[1].name).toEqual('jsonExample');

      // Actions
      getController(true);
      $scope.page.variables = {
        aKey: {type: 'constant', displayValue: 'aValue'},
        jsonExample: {type: 'json', displayValue: '{}'},
        urlExample: {type: 'url', displayValue: 'https://api.github.com/users/jnizet'},
        jsExample: {type: 'expression', displayValue: 'return true'}
      };
      let actions = $scope.getVariables();
      expect(actions).toEqual([
        {type: 'url', displayValue: 'https://api.github.com/users/jnizet'},
        {type: 'expression', displayValue: 'return true'}
      ]);
      // each object should have a property non enumerable named name
      expect(actions[0].name).toEqual('urlExample');
    });

    it('should filter variables by name', function () {
      getController(false);
      $scope.page.variables = {
        aKey: {type: 'constant', displayValue: 'aValue'},
        jsonExample: {type: 'json', displayValue: '{}'},
        urlExample: {type: 'url', displayValue: 'https://api.github.com/users/jnizet'}
      };

      let filtered = $scope.getVariables('aKey');
      expect(filtered).toEqual([{type: 'constant', displayValue: 'aValue'}]);

      filtered = $scope.getVariables('unknownkey');
      expect(filtered).toEqual([]);
    });

    it('should filter variables by value', function() {
      getController(false);
      $scope.page.variables = {
        aKey: {type: 'constant', displayValue: 'aValue'},
        jsonExample: {type: 'json', displayValue: '{}'},
        urlExample: {type: 'url', displayValue: 'https://api.github.com/users/jnizet'}
      };

      let filtered = $scope.getVariables('aValue');
      expect(filtered).toEqual([{type: 'constant', displayValue: 'aValue'}]);

      filtered = $scope.getVariables('unknownvalue');
      expect(filtered).toEqual([]);
    });

    it('should open a data popup', function() {
      getController(false);
      spyOn($uibModal, 'open').and.returnValue({
        result: $q.when({})
      });
      $scope.openDataPopup();
      expect($uibModal.open).toHaveBeenCalled();
    });

    it('should return none exposed data type', function() {
      getController(false);
      var data = {
        $$name: 'colin',
        displayValue: 4,
        exposed: false,
        type: 'constant'
      };

      expect($scope.getType(data)).toEqual('String');
    });

    it('should return (Exposed) for exposed data type', function() {
      getController(false);
      let data = {
        $$name: 'colin',
        displayValue: 4,
        exposed: true,
        type: 'constant'
      };
      $scope.exposableData = true ;

      expect($scope.getType(data)).toEqual('(Exposed)');
    });

    it('should sort based on a sort criteria', function() {
      getController(false);

      $scope.sort('aCriteria');

      expect($scope.sortCriteria).toBe('aCriteria');
    });

    it('should change sort order while sorting twice on same criteria', function() {
      getController(false);

      $scope.sort('aCriteria');
      expect($scope.isReversedSorting).toBe(false);

      $scope.sort('aCriteria');
      expect($scope.isReversedSorting).toBe(true);
    });

    it('should reset sort order while changing sort criteria', function() {
      getController(false);
      $scope.isReversedSorting = true;
      $scope.sortCriteria = 'aCriteria';

      $scope.sort('anotherCriteria');

      expect($scope.isReversedSorting).toBe(false);
      expect($scope.sortCriteria).toBe('anotherCriteria');
    });

    it('should be sorted by name by default', function() {
      getController(false);
      expect($scope.sortCriteria).toBe('name');
    });

  });
})();
