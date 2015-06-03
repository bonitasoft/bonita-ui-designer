(function () {
  'use strict'

  describe('assetType filter', function(){
    var $filter, assets, filters;

    beforeEach(module('pb.assets'));

    beforeEach(inject(function(_$filter_){
      $filter = _$filter_;

      assets = [
        { "name": "MyAbcExample.js", "type": "js" },
        { "name": "MyAbcExample.css", "type": "css" },
        { "name": "MyAbcExample.png", "type": "img" }
      ];

      filters = [
        { key: 'js', label: 'JavaScript', filter: true},
        { key: 'css', label: 'CSS', filter: true},
        { key: 'img', label: 'Image', filter: true}
      ];
    }));

    function filterOnItem(item){
      filters.filter(function(elt){
        return elt.key === item;
      })[0].filter = false;
    }

    it('should not filter when no arg filters', function() {
      expect($filter('assetFilter')(assets)).toEqual(assets);
    });

    it('should not filter when filtering items are true', function() {
      expect($filter('assetFilter')(assets, filters)).toEqual(assets);
    });

    it('should exlude css', function() {
      filterOnItem('css');

      expect($filter('assetFilter')(assets, filters)).toEqual([
        { "name": "MyAbcExample.js", "type": "js" },
        { "name": "MyAbcExample.png", "type": "img" }
      ]);
    });

    it('should exlude img', function() {
      filterOnItem('img');

      expect($filter('assetFilter')(assets, filters)).toEqual([
        { "name": "MyAbcExample.js", "type": "js" },
        { "name": "MyAbcExample.css", "type": "css" }
      ]);
    });

    it('should exlude js', function() {
      filterOnItem('js');

      expect($filter('assetFilter')(assets, filters)).toEqual([
        { "name": "MyAbcExample.css", "type": "css" },
        { "name": "MyAbcExample.png", "type": "img" }
      ]);
    });
  });

})();