(function() {
  'use strict';

  describe('AssetCtrl', function() {
    var $scope, $q, $uibModal, assetsService, artifactRepo, controller, component;

    beforeEach(angular.mock.module('bonitasoft.designer.assets'));

    beforeEach(inject(function($injector) {
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
        },
        deleteAsset: function() {
        },
        createAsset: function() {
        }
      };
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
          assetsService: assetsService
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

      it('should delete an asset', function() {
        var asset = { name: 'myasset.js' };

        spyOn(artifactRepo, 'deleteAsset').and.returnValue($q.when({}));

        controller.delete(asset);
        $scope.$apply();

        expect(artifactRepo.deleteAsset).toHaveBeenCalledWith(12, asset);
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

    });

    describe('Widget editor', function() {

      beforeEach(inject(function($injector) {
        $scope.widget = {};
        controller = $injector.get('$controller')('AssetCtrl', {
          $scope: $scope,
          artifact: component,
          artifactRepo: artifactRepo,
          mode: 'widget',
          assetsService: assetsService
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
