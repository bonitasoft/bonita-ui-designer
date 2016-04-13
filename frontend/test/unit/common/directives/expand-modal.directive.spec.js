describe('ExpandModal', () => {
  let element, scope;
  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));
  beforeEach(inject(($compile, $rootScope) => {
    scope = $rootScope.$new();
    element = $compile('<div class="modal-dialog"><expand-modal></expand-modal></div>')(scope);
    scope.$apply();
  }));

  it('should add a expand button', () => {
    expect(element.hasClass('modal-xxl')).toBeFalsy();
    expect(element.find('i').hasClass('fa-expand')).toBeTruthy();
    expect(element.find('i').hasClass('fa-compress')).toBeFalsy();
    element.find('button').click();
    scope.$apply();

    expect(element.find('i').hasClass('fa-compress')).toBeTruthy();
    expect(element.find('i').hasClass('fa-expand')).toBeFalsy();
    expect(element.hasClass('modal-xxl')).toBeTruthy();
    element.find('button').click();
    scope.$apply();

    expect(element.hasClass('modal-xxl')).toBeFalsy();
  });
});
