(function() {
  'use strict';
  describe('DataPopupController', function() {

    let $scope, $uibModalInstance, getController;

    beforeEach(angular.mock.module('bonitasoft.designer.editor.bottom-panel.data-panel', 'mock.modal'));

    beforeEach(inject(function($injector) {
      $scope = $injector.get('$rootScope').$new();

      $uibModalInstance = $injector.get('$uibModalInstance').create();

      let $controller = $injector.get('$controller');

      getController = function(pageData, data, isAction) {
        return $controller('DataPopupController', {
          $scope: $scope,
          $uibModalInstance: $uibModalInstance,
          mode: 'page',
          pageData: pageData,
          data: data,
          isAction: isAction
        });
      };
    }));

    describe('Variable creation', function() {
      let pageData = { users: { value: [] } };
      let data;
      let isAction = false;

      beforeEach(function() {
        getController(pageData, data, isAction);
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
        $scope.variableDataTypes.forEach(function(dataType) {
          $scope.updateValue(dataType.type);
          expect($scope.newData.displayValue).toBe(dataType.defaultValue);
        });
      });
    });

    describe('Edit variable', function() {
      let data = { $$name: 'users', displayValue: '4', type: 'constant' };
      let pageData = { users: { displayValue: '4' } };
      let isAction = false;

      beforeEach(function() {
        getController(pageData, data, isAction);
      });

      it('should init scope data', function() {

        expect($scope.pageData).toBe(pageData);
        expect($scope.newData).toEqual(data);
        expect($scope.isNewData).toBe(false);
      });
    });

    describe('save variable', function() {
      let pageData = { users: { value: ['4'] } };
      let data;
      let isAction = false;

      beforeEach(function() {
        getController(pageData, data, isAction);
      });

      it('should close modal', function() {
        let data = {
          $$name: 'user',
          type: 'constant',
          displayValue: 'silentBob'
        };

        $scope.save(data, 'silentBob');
        expect($uibModalInstance.close).toHaveBeenCalledWith(data);
      });
    });

    describe('Variable update', function() {

      let data = { $$name: 'users', displayValue: '4', type: 'url' };
      let pageData = { users: { displayValue: '4' }, userResponse: { displayValue: {} }, toto: { displayValue: {} }};
      let isAction = true;

      beforeEach(function() {
        getController(pageData, data, isAction);
      });

      it('should get list of all variable without himself', function() {
        let filterData = $scope.getDataNamesWithoutActualVariable();
        expect(filterData.length).toBe(2);
        expect(filterData['users']).toBe(undefined);
      });
    });
  });
})();
