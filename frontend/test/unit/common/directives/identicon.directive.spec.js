describe('IdenticonDirective', () => {

  let element, scope, $compile, $window;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));

  beforeEach(inject(($rootScope, _$compile_, _$window_) => {
    $window = _$window_;
    $compile = _$compile_;
    scope = $rootScope.$new();
  }));
  it('should show an base 64 image', () => {
    element = $compile('<div><identicon name="bonita" size="30"/></div>')(scope);
    scope.$apply(); 
    let img = element.find('img');
    expect(img.attr('height')).toEqual('30');
    expect(img.attr('width')).toEqual('30');
    expect(img.attr('ng-src')).toContain('data:image/png;base64,');
    expect(img.attr('ng-src')).toContain('PrRAS1MkMOhAAAAAElFTkSuQmCC');
  });
  describe('options', () => {
    beforeEach(() => {
      spyOn($window, 'Identicon').and.callThrough();
    });
    it('should have background color passed', () => {
      element = $compile('<div><identicon name="bonita" size="30" background-color="[60,60,60]"/></div>')(scope);
      scope.$apply(); 
      expect($window.Identicon).toHaveBeenCalledWith({ hash: 'bfa9e803ac1996bf71fe537e853fe67d4cfc19f3', size: 30, bg: [ 60, 60, 60 ], fg: [ 255, 255, 255 ] });
    });
    it('should have foreground color passed', () => {
      element = $compile('<div><identicon name="bonita" size="30" foreground-color="[60,60,60]"/></div>')(scope);
      scope.$apply(); 
      expect($window.Identicon).toHaveBeenCalledWith({ hash: 'bfa9e803ac1996bf71fe537e853fe67d4cfc19f3', size: 30, bg: [ 64, 72, 83 ], fg: [ 60, 60, 60 ] });
    });
  });
});
