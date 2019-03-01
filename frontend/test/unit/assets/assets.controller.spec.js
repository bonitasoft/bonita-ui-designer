(function () {
  'use strict';

  describe('AssetCtrl', function () {
    var $scope, $q, $uibModal, assetsService, artifactRepo, controller, component, assetRepo, assetEditPopup;

    beforeEach(angular.mock.module('bonitasoft.designer.assets'));

    beforeEach(inject(function ($injector, _assetEditPopup_) {
      $scope = $injector.get('$rootScope').$new();
      $q = $injector.get('$q');
      assetsService = $injector.get('assetsService');
      $uibModal = $injector.get('$uibModal');
      component = {id: 12, type:'page'};
      artifactRepo = {
        loadAssets: function () {
          return $q.when([
            {id: '123', name: 'myAsset', scope: 'PAGE', active: true},
            {id: '456', name: 'myPrivateDeactivatedAsset', scope: 'PAGE', active: false},
            {id: '789', name: 'publicAsset', scope: 'WIDGET', active: true},
            {id: '321', name: 'publicDeactivatedAsset', scope: 'WIDGET', active: false}
          ]);
        }
      };
      assetRepo = jasmine.createSpyObj('assetRepo', ['deleteAsset', 'createAsset']);
      assetEditPopup = _assetEditPopup_;
    }));

    describe('Page editor', function () {

      beforeEach(inject(function ($injector) {
        $scope.page = {};
        controller = $injector.get('$controller')('AssetCtrl', {
          $scope: $scope,
          $uibModal: $uibModal,
          artifact: component,
          artifactRepo: artifactRepo,
          mode: 'page',
          assetsService: assetsService,
          assetRepo: assetRepo
        });

        spyOn($uibModal, 'open').and.returnValue({
          result: $q.when({})
        });
      }));

      it('should expose types', function () {
        $scope.$digest();
        expect(controller.types).toEqual({
          css: {label: 'CSS', value: true, orderable: true},
          js: {label: 'JavaScript', value: true, orderable: true},
          img: {label: 'Image', value: true, orderable: false}
        });
      });

      it('should expose scopeFilter', function () {
        $scope.$digest();
        expect(controller.scopeFilter).toEqual({
          page: {key: 'page', value: 'Page', filter: true},
          widget: {key: 'widget', value: 'Widget', filter: false},
          baseFramework: { key: 'baseFramework', value: 'Base Framework', filter: false}
        });
      });

      describe('getAssetsByTypeForCurrentScope', function() {
        beforeEach(inject(function () {
          spyOn(assetsService, 'getBaseFrameworkAsset').and.returnValue([{
            active: true,
            external: false,
            name: 'angular-1.3.18.js',
            order: 0,
            scope: 'baseFramework',
            type: 'js'
          }, {
            active: true,
            external: false,
            name: 'bootstrap-3.3.6.css',
            order: -1,
            scope: 'baseFramework',
            type: 'css'
          }, {
            active: true,
            external: true,
            name: '../theme/theme.css',
            order: -2,
            scope: 'baseFramework',
            type: 'css'
          }]);
        }));

        it('should return js baseFramework asset when baseFramework is selected', function () {
          $scope.$digest();
          controller.scopeFilter.baseFramework.filter = true;
          expect(controller.getAssetsByTypeForCurrentScope('js')).toEqual([{ active: true, external: false, name: 'angular-1.3.18.js', order: 0, scope: 'baseFramework', type: 'js' }]);
        });

        it('should not return js baseFramework asset when baseFramework is selected and searchTerm is relevant', function () {
          $scope.searchTerm = '1.3.1';

          $scope.$digest();
          controller.scopeFilter.baseFramework.filter = true;
          expect(controller.getAssetsByTypeForCurrentScope('js')).toEqual([{ active: true, external: false, name: 'angular-1.3.18.js', order: 0, scope: 'baseFramework', type: 'js' }]);
        });

        it('should not return js baseFramework asset when baseFramework is selected and searchTerm is not relevant', function () {
          $scope.searchTerm = 'react';

          $scope.$digest();
          controller.scopeFilter.baseFramework.filter = true;
          expect(controller.getAssetsByTypeForCurrentScope('js')).toEqual([]);
        });
      });

      it('should open a data popup for asset preview', function () {
        controller.openAssetPreviewPopup();
        expect($uibModal.open).toHaveBeenCalled();
      });

      it('should open a data popup for asset management', function () {
        controller.openAssetPopup();
        expect($uibModal.open).toHaveBeenCalled();
      });

      it('should open update asset popup while editing an external asset', () => {
        spyOn(assetsService, 'isExternal').and.returnValue(true);

        controller.openAssetEditPopup();

        expect($uibModal.open).toHaveBeenCalled();
        expect($uibModal.open.calls.mostRecent().args[0].templateUrl).toEqual('js/assets/asset-popup.html');
        expect($uibModal.open.calls.mostRecent().args[0].controller).toEqual('AssetPopupCtrl');
      });

      it('should open edit asset popup while editing a local asset', () => {
        spyOn(assetsService, 'isExternal').and.returnValue(false);
        spyOn(assetEditPopup, 'open');
        let asset = {id: 'anAsset'};

        controller.openAssetEditPopup(asset);

        expect(assetEditPopup.open).toHaveBeenCalledWith({asset, assetRepo, component});
      });

      it('should delete an asset', function () {
        var asset = {name: 'myasset.js'};
        assetRepo.deleteAsset.and.returnValue($q.when({}));

        controller.delete(asset);
        $scope.$apply();

        expect(assetRepo.deleteAsset).toHaveBeenCalledWith(12, asset);
      });

      it('should get url for widget asset in page mode', function () {
        var asset = {name: 'myasset.js', scope: 'widget', type: 'js', componentId: '11'};
        var assetUrl = controller.getAssetUrl(asset);

        expect(assetUrl).toBe('rest/widgets/11/assets/js/myasset.js');
      });

      it('should get page asset url', function () {
        var asset = {name: 'myasset.js', scope: 'page', type: 'js'};
        var assetUrl = controller.getAssetUrl(asset);

        expect(assetUrl).toBe('rest/pages/12/assets/js/myasset.js');
      });

      it('should say if an asset is editable or not in page-editor', function () {
        expect(controller.isEditable({type: 'img', scope: 'page'})).toBeFalsy();
        expect(controller.isEditable({type: 'img', scope: 'widget'})).toBeFalsy();
        expect(controller.isEditable({type: 'css', scope: 'page'})).toBeTruthy();
        expect(controller.isEditable({type: 'css', scope: 'widget'})).toBeFalsy();
        expect(controller.isEditable({type: 'js', scope: 'page'})).toBeTruthy();
        expect(controller.isEditable({type: 'js', scope: 'widget'})).toBeFalsy();
      });

      it('should say if an asset is viewable or not in page-editor', function () {
        expect(controller.isViewable({external: true})).toBeFalsy();
        expect(controller.isViewable({type: 'img'})).toBeTruthy();
        expect(controller.isViewable({type: 'css', scope: 'widget'})).toBeTruthy();
      });

    });

    describe('Widget editor', function () {

      beforeEach(inject(function ($injector) {
        $scope.widget = {};
        var component = {id: 12, type: 'widget'};
        controller = $injector.get('$controller')('AssetCtrl', {
          $scope: $scope,
          artifact: component,
          artifactRepo: artifactRepo,
          mode: 'widget',
          assetsService: assetsService,
          assetRepo: assetRepo
        });
      }));

      it('should open a data popup for asset preview', function () {
        spyOn($uibModal, 'open').and.returnValue({
          result: $q.when({})
        });
        controller.openAssetPreviewPopup();
        expect($uibModal.open).toHaveBeenCalled();
      });

      it('should open a data popup for asset management', function () {
        spyOn($uibModal, 'open').and.returnValue({
          result: $q.when({})
        });
        controller.openAssetPopup();
        expect($uibModal.open).toHaveBeenCalled();
      });

      it('should open a data popup for asset preview', function () {
        spyOn($uibModal, 'open').and.returnValue({
          result: $q.when({})
        });
        controller.openAssetPreviewPopup();
        expect($uibModal.open).toHaveBeenCalled();
      });

      it('should get url for widget mode', function () {
        var asset = {name: 'myasset.js', scope: 'page', type: 'js'};
        var assetUrl = controller.getAssetUrl(asset);

        expect(assetUrl).toBe('rest/widgets/12/assets/js/myasset.js');
      });

      it('should say if an asset is editable or not in widget-editor', function () {
        expect(controller.isEditable({type: 'img'})).toBeFalsy();
        expect(controller.isEditable({type: 'css'})).toBeTruthy();
        expect(controller.isEditable({type: 'js'})).toBeTruthy();
        expect(controller.isEditable({type: 'js', scope: 'page'})).toBeFalsy();
        expect(controller.isEditable({type: 'js', scope: 'widget'})).toBeTruthy();
      });

      it('should say if an asset is viewable or not', function () {
        expect(controller.isViewable({external: true})).toBeFalsy();
        expect(controller.isViewable({type: 'img'})).toBeTruthy();
        expect(controller.isViewable({type: 'css', scope: 'widget'})).toBeFalsy();
        expect(controller.isViewable({type: 'img', scope: 'widget'})).toBeTruthy();
      });
    });
  });
})();
