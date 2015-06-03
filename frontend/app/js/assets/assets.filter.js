(function() {

  /**
   * Filters the assets table in editor by type
   */
  angular.module('pb.assets').filter('assetFilter', function() {

    'use strict';

    return function(assets, filters){
      if(assets && filters){
        var assetsFiltered = assets.filter(function(asset){
          var assetType = filters.filter(function (elt) {
            return elt.key === asset.type;
          })[0];
          return assetType ? assetType.filter : false;
        });
        return assetsFiltered;
      }
      return assets;
    };
  });

})();