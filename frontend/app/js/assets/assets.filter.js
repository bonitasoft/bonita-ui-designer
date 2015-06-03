(function() {

  /**
   * Filters the assets table in editor by type
   */
  angular.module('pb.assets').filter('assetFilter', function() {

    'use strict';

    return function(assets, filters){
      if(!assets || !filters){
        return assets;
      }
      return assets.filter(function(asset){
        return filters.filter(function (elt) {
          return elt.key === asset.type;
        })[0].filter;
      });
    };
  });

})();