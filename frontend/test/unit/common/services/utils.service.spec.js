describe('utils', function() {
  var utils;

  beforeEach(angular.mock.module('bonitasoft.designer.common.services'));
  beforeEach(inject(function($injector) {
    utils = $injector.get('utils');
  }));

  describe('clamp', function() {
    it('should return a clamp value', function() {
      var min = 0;
      var max = 5;
      expect(utils.clamp(min, 3, max)).toBe(3);
      expect(utils.clamp(min, -1, max)).toBe(min);
      expect(utils.clamp(min, 10, max)).toBe(max);
    });
  });
});
