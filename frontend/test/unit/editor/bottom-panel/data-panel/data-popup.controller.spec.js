(function() {
  'use strict';
  describe('DataPopupController', function() {

    var $scope, $location, $uibModalInstance, getController;

    beforeEach(angular.mock.module('bonitasoft.designer.editor.bottom-panel.data-panel', 'mock.modal'));

    beforeEach(inject(function($injector) {
      $scope = $injector.get('$rootScope').$new();

      $uibModalInstance = $injector.get('$uibModalInstance').create();

      $location = $injector.get('$location');
      var $controller = $injector.get('$controller');

      getController = function(pageData, data) {
        return $controller('DataPopupController', {
          $scope: $scope,
          $uibModalInstance: $uibModalInstance,
          mode: 'page',
          pageData: pageData,
          data: data
        });
      };
    }));

    describe('Variable creation', function() {
      var pageData = { users: { value: [] } };
      var data;
      var controller;

      beforeEach(function() {
        controller = getController(pageData, data);
      });

      it('should init scope data', function() {

        expect($scope.pageData).toBe(pageData);
        expect($scope.newData).toEqual({ type: 'constant', exposed: false });
        expect($scope.isNewData).toBe(true);
      });

      it('should check variable name uniqness', function() {
        expect($scope.isDataNameUnique('users')).toBe(false);
        expect($scope.isDataNameUnique('toto')).toBe(true);
      });

      it('should init newData.displayValue depending on the selectedType', function() {
        $scope.dataTypes.forEach(function(dataType) {
          $scope.updateValue(dataType.type);
          expect($scope.newData.displayValue).toBe(dataType.defaultValue);
        });
      });
    });

    describe('Edit variable', function() {
      var data = { $$name: 'users', displayValue: '4', type: 'constant' };
      var pageData = { users: { displayValue: '4' } };
      var controller;

      beforeEach(function() {
        controller = getController(pageData, data);
      });

      it('should init scope data', function() {

        expect($scope.pageData).toBe(pageData);
        expect($scope.newData).toEqual(data);
        expect($scope.isNewData).toBe(false);
      });
    });

    describe('save variable', function() {
      var pageData = { users: { value: ['4'] } };
      var data;
      var controller;

      beforeEach(function() {
        controller = getController(pageData, data);
      });

      it('should close modal', function() {
        var data = {
          $$name: 'user',
          type: 'constant',
          displayValue: 'silentBob'
        };

        $scope.save(data, 'silentBob');
        expect($uibModalInstance.close).toHaveBeenCalledWith(data);
      });
    });
  });
})();
