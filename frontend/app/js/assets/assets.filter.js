(function() {

  angular.module('pb.assets').filter('assetType', function(assetsService) {

    'use strict';

    return function(key){
      return assetsService.getTypeLabel(key);
    };
  });

})();