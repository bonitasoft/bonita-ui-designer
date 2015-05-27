(function () {
  'use strict'

  describe('AssetCtrl', function(){
    var $scope, $q, assets;

    beforeEach(module('pb.assets', 'ui.bootstrap'));

    beforeEach(inject(function($injector) {
      $scope = $injector.get('$rootScope').$new();
      $q = $injector.get('$q');
      assets = $injector.get('assets');

      $injector.get('$controller')('AssetCtrl', {
        $scope: $scope,
        artifact: {},
        artifactRepo: {},
        mode : 'page',
        assets: assets
      });

    }));

    it('should put artifact in $scope', function(){
      expect($scope.component).toEqual({});
    });

    describe('filter on assets', function(){

      it('should do nothing by default', function(){
        expect($scope.filterBySearchedAsset({ "name": "MyAbcExample.js", "type": "js" })).toBeTruthy();
        expect($scope.filterBySearchedAsset({ "name": "MyAbcExample.css", "type": "css" })).toBeTruthy();
        expect($scope.filterBySearchedAsset({ "name": "MyAbcExample.png", "type": "img" })).toBeTruthy();
      });

      it('should return false when asset type undefined', function(){
        expect($scope.filterBySearchedAsset({ "name": "MyAbcExample.js", "type": undefined })).toBeFalsy();
      });

      it('should skip other types if I want a filter only on js', function(){
        $scope.searchedAsset = $scope.searchedAsset.map(function(elt){
          if(elt.key!=='js'){
            elt.filter=false;
          }
          return elt;
        });

        expect($scope.filterBySearchedAsset({ "name": "MyAbcExample.js", "type": "js" })).toBeTruthy();
        expect($scope.filterBySearchedAsset({ "name": "MyAbcExample.css", "type": "css" })).toBeFalsy();
        expect($scope.filterBySearchedAsset({ "name": "MyAbcExample.png", "type": "img" })).toBeFalsy();
      });

      it('should skip all types if filter are all actives ', function(){
        $scope.searchedAsset = $scope.searchedAsset.map(function(elt){
          elt.filter=false;
          return elt;
        });

        expect($scope.filterBySearchedAsset({ "name": "MyAbcExample.js", "type": "js" })).toBeFalsy();
        expect($scope.filterBySearchedAsset({ "name": "MyAbcExample.css", "type": "css" })).toBeFalsy();
        expect($scope.filterBySearchedAsset({ "name": "MyAbcExample.png", "type": "img" })).toBeFalsy();
      });
    });
  });

})();