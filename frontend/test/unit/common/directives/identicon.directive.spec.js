describe('IdenticonDirective', () => {

  var element;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));

  beforeEach(inject(($rootScope, $compile) => {
    let scope = $rootScope.$new();
     element = $compile('<div><identicon name="bonita" size="30"/></div>')(scope);
     scope.$apply(); 
  }));
  it('should show an base 64 image', () => {
    let img = element.find('img');
    expect(img.attr('height')).toEqual('30');
    expect(img.attr('width')).toEqual('30');
    expect(img.attr('ng-src')).toContain('data:image/png;base64,');
    expect(img.attr('ng-src')).toContain('PrRAS1MkMOhAAAAAElFTkSuQmCC');
  });
});
