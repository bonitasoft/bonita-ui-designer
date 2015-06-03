(function () {
  'use strict'

  describe('AssetCtrl', function () {
    var $scope, $q, $modal, assetsService, artifactRepo, controller;

    beforeEach(module('pb.assets', 'ui.bootstrap'));

    beforeEach(inject(function ($injector) {
      $scope = $injector.get('$rootScope').$new();
      $q = $injector.get('$q');
      assetsService = $injector.get('assetsService');
      $modal = $injector.get('$modal');
      artifactRepo = {
        loadAssets: function () {
          return $q.when([
            {name: 'myAsset'}
          ]);
        },
        deleteAsset: function () {
        },
        createAsset: function () {
        }
      };
    }));

    describe('Page editor', function () {

      beforeEach(inject(function ($injector) {
        $scope.page= {};
        controller = $injector.get('$controller')('AssetCtrl', {
          $scope: $scope,
          $modal: $modal,
          artifact: { id: 12},
          artifactRepo: artifactRepo,
          mode: 'page',
          assetsService: assetsService
        });
      }));

      it('should put assets in $scope', function () {
        $scope.$digest();
        expect($scope.assets).toEqual([
          {name: 'myAsset'}
        ]);
      });

      it('should open a data popup for asset preview', function () {
        spyOn($modal, 'open').and.returnValue({
          result: $q.when({})
        });
        $scope.openAssetPreviewPopup();
        expect($modal.open).toHaveBeenCalled()
      });

      it('should open a data popup for asset management', function () {
        spyOn($modal, 'open').and.returnValue({
          result: $q.when({})
        });
        $scope.openAssetPopup();
        expect($modal.open).toHaveBeenCalled()
      });

      it('should delete an asset', function () {
        var asset = {name: 'myasset.js'};

        spyOn(artifactRepo, 'deleteAsset').and.returnValue($q.when({}));

        $scope.delete(asset);
        $scope.$apply();

        expect(artifactRepo.deleteAsset).toHaveBeenCalledWith(12, asset);
      });

      it('should return an empty promise when arg datas is undefined in createOrUpdate', function () {
        expect(controller.createOrUpdate()).toEqual( $q.when({}));
      });

      it('should create a new external asset', function () {
        var asset = {name: 'myasset.js', isNew: true};
        spyOn(artifactRepo, 'createAsset').and.returnValue($q.when({}));

        controller.createOrUpdate(asset);

        expect(artifactRepo.createAsset).toHaveBeenCalled();
      });

      it('should create an existing external asset', function () {
        var asset = {name: 'myasset.js', oldname: 'myoldasset.js'};
        spyOn(artifactRepo, 'deleteAsset').and.returnValue($q.when({}));
        spyOn(artifactRepo, 'createAsset').and.returnValue($q.when({}));

        controller.createOrUpdate(asset);

        expect(artifactRepo.deleteAsset).toHaveBeenCalled();
      });

    });


    describe('Widget editor', function () {

      beforeEach(inject(function ($injector) {
        $scope.widget= {};
        controller = $injector.get('$controller')('AssetCtrl', {
          $scope: $scope,
          artifact: {},
          artifactRepo: artifactRepo,
          mode: 'widget',
          assetsService: assetsService
        });
      }));

      it('should put assets in $scope', function () {
        $scope.$digest();
        expect($scope.assets).toEqual([
          {name: 'myAsset'}
        ]);
      });

      it('should open a data popup for asset preview', function () {
        spyOn($modal, 'open').and.returnValue({
          result: $q.when({})
        });
        $scope.openAssetPreviewPopup();
        expect($modal.open).toHaveBeenCalled()
      });

      it('should open a data popup for asset management', function () {
        spyOn($modal, 'open').and.returnValue({
          result: $q.when({})
        });
        $scope.openAssetPopup();
        expect($modal.open).toHaveBeenCalled()
      });

      it('should open a data popup for asset preview', function () {
        spyOn($modal, 'open').and.returnValue({
          result: $q.when({})
        });
        $scope.openAssetPreviewPopup();
        expect($modal.open).toHaveBeenCalled()
      });


    });



  });
})();