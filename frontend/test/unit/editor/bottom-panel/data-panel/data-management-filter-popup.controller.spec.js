(function () {
  'use strict';
  describe('DataManagementFilterPopupController', function () {

    let $uibModalInstance, getController;

    beforeEach(angular.mock.module('bonitasoft.designer.editor.bottom-panel.data-panel', 'mock.modal'));

    beforeEach(inject(function ($injector) {

      let $scope = $injector.get('$rootScope').$new();
      let gettextCatalog = $injector.get('gettextCatalog');
      let businessDataUpdateService = $injector.get('businessDataUpdateService');
      $uibModalInstance = $injector.get('$uibModalInstance').create();
      let $controller = $injector.get('$controller');

      getController = function (businessData, queriesForObject, pageData) {
        return $controller('DataManagementPopupController', {
          $scope: $scope,
          $uibModalInstance: $uibModalInstance,
          businessData: businessData,
          queriesForObject: queriesForObject,
          pageData: pageData,
          businessDataUpdateService: businessDataUpdateService,
          gettextCatalog: gettextCatalog
        });
      };
    }));

    describe('Variable creation', function () {
      let pageData = {
        ctxMetier: {
          type: 'businessdata',
          displayValue: '{"displayValue":"findByIdCtxMetier [com.origin.mod…ue":"{{metierId}}"}],"pagination":{"p":0,"c":10}}',
          exposed: false,
          name: 'ctxMetier'
        },
        metierId: {type: 'constant', displayValue: '1', exposed: false, name: 'metierId'},
        metierId_1: {type: 'constant', displayValue: '1', exposed: false, name: 'metierId_1'} //jshint ignore:line
      };
      let queriesForObject = {
        additionalQuery: [
          {displayName: 'find', query: 'find', filters: []},
          {
            displayName: 'findBylibelleFRAndidCtxContratType', query: 'findBylibelleFRAndidCtxContratType',
            filters: [{name: 'libelleFR'}, {name: 'idCtxContratType'}]
          }],
        defaultQuery: [
          {displayName: 'idCtxContratType', query: 'findByIdCtxContratType', filters: [{name: 'idCtxContratType'}]},
          {displayName: 'libelleFR', query: 'findByLibelleFR', filters: [{name: 'libelleFR'}]},
          {displayName: 'persistenceId', query: 'findByPersistenceId', filters: [{name: 'persistenceId'}]}
        ]
      };
      let businessData = {
        id: 'com_origin_model_CtxContratType',
        name: 'CtxContratType',
        qualifiedName: 'com.origin.model.CtxContratType',
        type: 'model'
      };

      let controller;

      beforeEach(function () {
        controller = getController(businessData, queriesForObject, pageData);
      });

      it('should return an error when variable name already exist', function () {
        let alreadyExist = controller.isDataNameUnique('ctxMetier');

        expect(alreadyExist).toBe(false);

        let isUnique = controller.isDataNameUnique('ctxFonction');

        expect(isUnique).toBe(true);
      });

      it('should generate a new variable name when variable name already exist', function () {
        let generateName = controller.generateVariableName('ctxMetier');

        expect(generateName).toBe('ctxMetier_1');
      });

      it('should generate a new variable name with suffix incrementation when variable name already exist', function () {
        let generateName = controller.generateVariableName('metierId');

        expect(generateName).toBe('metierId_2');
      });

      it('should create a new variable when save button is click', function () {
        controller.save();

        expect(controller.pageData.ctxContratType).toEqual({
          exposed: false,
          type: 'businessdata',
          displayValue: '{}'
        });
      });
    });
  });
})();
