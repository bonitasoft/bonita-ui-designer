(function () {
  'use strict'

  describe('assetType filter', function(){
    var $filter, assets, filters;

    beforeEach(module('bonitasoft.designer.assets'));

    beforeEach(inject(function(_$filter_){
      $filter = _$filter_;

      assets = [
        { "name": "MyAbcExample.js", "type": "js" },
        { "name": "MyAbcExample.css", "type": "css" },
        { "name": "MyAbcExample.png", "type": "img" }
      ];

      filters = {
        js : {key : 'js', value: 'JavaScript', filter:true },
        css : {key : 'css', value: 'CSS', filter:true},
        img : {key : 'img', value: 'Image', filter:true}
      };

    }));

    it('should not filter when no arg filters', function() {
      expect($filter('assetFilter')(assets)).toEqual(assets);
    });

    it('should not filter when filtering items are true', function() {
      expect($filter('assetFilter')(assets, filters)).toEqual(assets);
    });

    it('should exlude css', function() {
      filters.css.filter = false;

      expect($filter('assetFilter')(assets, filters)).toEqual([
        { "name": "MyAbcExample.js", "type": "js" },
        { "name": "MyAbcExample.png", "type": "img" }
      ]);
    });

    it('should exlude img', function() {
      filters.img.filter = false;

      expect($filter('assetFilter')(assets, filters)).toEqual([
        { "name": "MyAbcExample.js", "type": "js" },
        { "name": "MyAbcExample.css", "type": "css" }
      ]);
    });

    it('should exlude js', function() {
      filters.js.filter = false;;

      expect($filter('assetFilter')(assets, filters)).toEqual([
        { "name": "MyAbcExample.css", "type": "css" },
        { "name": "MyAbcExample.png", "type": "img" }
      ]);
    });
  });

})();
