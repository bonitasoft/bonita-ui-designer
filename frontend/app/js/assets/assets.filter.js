(function() {

  angular.module('pb.assets').filter('assetType', function(assets) {

    'use strict';

    return function(key){
      return assets.getLabel(key);
    };
  });

})();