(function() {
  'use strict';

  describe('assetType filter', function() {
    var $filter;

    beforeEach(angular.mock.module('bonitasoft.designer.assets'));

    beforeEach(inject(function(_$filter_) {
      $filter = _$filter_;
    }));

    it('should return "JavaScript" when given js', function() {
      expect($filter('assetType')('js')).toEqual('JavaScript');
    });

    it('should return "CSS" when given css', function() {
      expect($filter('assetType')('css')).toEqual('CSS');
    });

    it('should return "Images" when given img', function() {
      expect($filter('assetType')('img')).toEqual('Image');
    });

    it('should return "" when given undefined', function() {
      expect($filter('assetType')()).toEqual('');
    });

    it('should return "" when given unknown', function() {
      expect($filter('assetType')('sdsdd')).toEqual('');
    });
  });

})();
