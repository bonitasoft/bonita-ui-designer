(function() {
  'use strict';

  describe('AssetCtrl', function() {
    var $scope, $q, $uibModal, assetsService, artifactRepo, controller, component, assetRepo, assetEditPopup;

    beforeEach(angular.mock.module('bonitasoft.designer.assets'));

    beforeEach(inject(function($injector, _assetEditPopup_) {
      $scope = $injector.get('$rootScope').$new();
      $q = $injector.get('$q');
      assetsService = $injector.get('assetsService');
      $uibModal = $injector.get('$uibModal');
      component = { id: 12 };
      artifactRepo = {
        loadAssets: function() {
          return $q.when([
            { id: '123', name: 'myAsset', scope: 'PAGE', active: true },
            { id: '456', name: 'myPrivateDeactivatedAsset', scope: 'PAGE', active: false },
            { id: '789', name: 'publicAsset', scope: 'WIDGET', active: true },
            { id: '321', name: 'publicDeactivatedAsset', scope: 'WIDGET', active: false }
          ]);
        }
      };
      assetRepo = jasmine.createSpyObj('assetRepo', ['deleteAsset', 'createAsset']);
      assetEditPopup = _assetEditPopup_;
    }));

    describe('Page editor', function() {

      beforeEach(inject(function($injector) {
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

      it('should expose filters', function() {
        $scope.$digest();
        expect(controller.filters).toEqual({
          js: { label: 'JavaScript', value: true },
          css: { label: 'CSS', value: true },
          img: { label: 'Image', value: true }
        });
      });

      it('should open a data popup for asset preview', function() {
        controller.openAssetPreviewPopup();
        expect($uibModal.open).toHaveBeenCalled();
      });

      it('should open a data popup for asset management', function() {
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

        expect(assetEditPopup.open).toHaveBeenCalledWith({ asset, assetRepo, component });
      });

      it('should delete an asset', function() {
        var asset = { name: 'myasset.js' };
        assetRepo.deleteAsset.and.returnValue($q.when({}));

        controller.delete(asset);
        $scope.$apply();

        expect(assetRepo.deleteAsset).toHaveBeenCalledWith(12, asset);
      });

      it('should get url for widget asset in page mode', function() {
        var asset = { name: 'myasset.js', scope: 'widget', type: 'js', componentId: '11' };
        var assetUrl = controller.getAssetUrl(asset);

        expect(assetUrl).toBe('rest/widgets/11/assets/js/myasset.js');
      });

      it('should get page asset url', function() {
        var asset = { name: 'myasset.js', scope: 'page', type: 'js' };
        var assetUrl = controller.getAssetUrl(asset);

        expect(assetUrl).toBe('rest/pages/12/assets/js/myasset.js');
      });

      it('should say if an asset is editable or not', function() {
        expect(controller.isEditable({type: 'img'})).toBeFalsy();
        expect(controller.isEditable({type: 'css'})).toBeTruthy();
        expect(controller.isEditable({type: 'js'})).toBeTruthy();
      });
    });

    describe('Widget editor', function() {

      beforeEach(inject(function($injector) {
        $scope.widget = {};
        var component = { id: 12, type: 'widget' };
        controller = $injector.get('$controller')('AssetCtrl', {
          $scope: $scope,
          artifact: component,
          artifactRepo: artifactRepo,
          mode: 'widget',
          assetsService: assetsService,
          assetRepo
        });
      }));

      it('should open a data popup for asset preview', function() {
        spyOn($uibModal, 'open').and.returnValue({
          result: $q.when({})
        });
        controller.openAssetPreviewPopup();
        expect($uibModal.open).toHaveBeenCalled();
      });

      it('should open a data popup for asset management', function() {
        spyOn($uibModal, 'open').and.returnValue({
          result: $q.when({})
        });
        controller.openAssetPopup();
        expect($uibModal.open).toHaveBeenCalled();
      });

      it('should open a data popup for asset preview', function() {
        spyOn($uibModal, 'open').and.returnValue({
          result: $q.when({})
        });
        controller.openAssetPreviewPopup();
        expect($uibModal.open).toHaveBeenCalled();
      });

      it('should get url for widget mode', function() {
        var asset = { name: 'myasset.js', scope: 'page', type: 'js' };
        var assetUrl = controller.getAssetUrl(asset);

        expect(assetUrl).toBe('rest/widgets/12/assets/js/myasset.js');
      });
    });

  });
})();
