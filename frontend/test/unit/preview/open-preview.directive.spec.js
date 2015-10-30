describe('directive openPreview', function() {

  'use strict';

  var scope, $window, $state, resolutions, dom, $compile;

  beforeEach(angular.mock.module('bonitasoft.designer.preview'));
  beforeEach(inject(function($injector) {

    $window = $injector.get('$window');
    $state = $injector.get('$state');
    scope = $injector.get('$rootScope').$new();
    resolutions = $injector.get('resolutions');
    $compile = $injector.get('$compile');

    spyOn($window, 'open');
    spyOn($state, 'href').and.returnValue('/preview?resolution=xs');
    spyOn(resolutions, 'selected').and.returnValue({ key: 'xs' });

  }));

  describe('directive default behaviour', function() {

    beforeEach(function() {
      dom = $compile('<button open-preview>open</button>')(scope);
    });

    it('should try to get the current preview url', function() {
      dom.click();
      expect($state.href).toHaveBeenCalledWith('designer.page.preview', {
        resolution: 'xs'
      });
    });

    it('should open a popup', function() {
      var params = 'width=1024,height=768,resizable=1,scrollbars=1';
      dom.click();
      expect($window.open).toHaveBeenCalledWith('/preview?resolution=xs', '_blank', params);
    });

  });

  describe('directive advanced behaviour', function() {

    beforeEach(function() {
      dom = $compile('<button open-preview popup-width="1280" popup-height="800">open</button>')(scope);
    });

    it('should try to get the current preview url', function() {
      dom.click();
      expect($state.href).toHaveBeenCalledWith('designer.page.preview', {
        resolution: 'xs'
      });
    });

    it('should open a popup', function() {
      var params = 'width=1280,height=800,resizable=1,scrollbars=1';
      dom.click();
      expect($window.open).toHaveBeenCalledWith('/preview?resolution=xs', '_blank', params);
    });

  });

});
