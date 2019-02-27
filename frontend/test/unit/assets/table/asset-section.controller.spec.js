/*******************************************************************************
 * Copyright (C) 2009, 2019 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
(function () {
  'use strict';

  describe('AssetSectionCtrl', function () {
    var $scope, $q, assetsService, controller, gettextCatalog, injector;

    beforeEach(angular.mock.module('bonitasoft.designer.assets'));

    beforeEach(inject(function ($injector) {
      injector = $injector;
      $scope = $injector.get('$rootScope').$new();
      $q = $injector.get('$q');
      gettextCatalog = $injector.get('gettextCatalog');
      assetsService = $injector.get('assetsService');
    }));

    function createController(scope) {
      controller = injector.get('$controller')('AssetSectionCtrl', {
        $scope: scope,
        assetsService: assetsService,
        gettextCatalog: gettextCatalog
      });
    }

    describe('getEmptyAssetMessage', function () {

      beforeEach(inject(function () {
        $scope = {
          type: 'js', id: 'js', assets: [
            {name: 'asset1'}, {name: 'asset2'}
          ],
          scopeFilter: {
            page: {filter: false},
            widget: {filter: false},
            baseFramework: {filter: false}
          }
        };
      }));

      it('should display specific message when no assets found', function () {
        $scope.assets = [];

        createController($scope);

        expect(controller.getEmptyAssetMessage('js', {label: 'javascript'})).toEqual('No javascript asset.');
      });

      it('should display specific message when no scope is selected', function () {
        createController($scope);

        expect(controller.getEmptyAssetMessage('js', {label: 'javascript'})).toEqual('No javascript asset (no scope selected).');
      });

      it('should display specific message when only page filter is active', function () {
        $scope.scopeFilter = {
          page: {filter: true},
          widget: {filter: false},
          baseFramework: {filter: false}
        };

        createController($scope);

        expect(controller.getEmptyAssetMessage('js', {label: 'javascript'})).toEqual('No javascript asset at page level.');
      });

      it('should display specific message when only widget filter is active', function () {
        $scope.scopeFilter = {
          page: {filter: false},
          widget: {filter: true},
          baseFramework: {filter: false}
        };

        createController($scope);

        expect(controller.getEmptyAssetMessage('js', {label: 'javascript'})).toEqual('No javascript asset at widget level.');
      });
    });

    describe('getAssetToDisplay', function(){
      beforeEach(inject(function () {
        $scope = {
          type: 'js', id: 'js', assets: [
            {name: 'asset1', scope: 'page'}, {name: 'asset2', scope: 'page'}
          ],
          scopeFilter: {
            page: {filter: true},
            widget: {filter: false},
            baseFramework: {filter: true}
          }
        };
        spyOn(assetsService, 'getBaseFrameworkAsset').and.returnValue([
        {
          active: true,
          external: false,
          name: 'angular-1.4.7.js',
          order: 0,
          scope: 'baseFramework',
          type: 'js'
        },{
          active: true,
          external: false,
          name: 'bootstrap-3.3.6.css',
          order: -1,
          scope: 'baseFramework',
          type: 'css'
        },
        {
          active: true,
          external: true,
          name: '../theme/theme.css',
          order: -2,
          scope: 'baseFramework',
          type: 'css'
        }]);
      }));

      it('should return js page asset and js baseFramework page asset when filter page and baseFramework is selected', function () {
        $scope.scopeFilter = {
          page: {filter: true},
          widget: {filter: false},
          baseFramework: {filter: true}
        };
        createController($scope);

        expect(controller.getAssetToDisplay()).toEqual([
          {name: 'asset1', scope: 'page'}, {name: 'asset2', scope: 'page'}, { active: true, external: false, name: 'angular-1.4.7.js', order: 0, scope: 'baseFramework', type: 'js' }
        ]);
      });
    });
  });
})();
