describe('open help directive', function() {
  var $compile, element, scope, directiveScope, $uibModal, modalInstance;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.header', 'mock.modal'));
  beforeEach(inject(function(_$compile_, $rootScope, _$uibModal_, $uibModalInstance) {
    $compile = _$compile_;

    scope = $rootScope.$new();
    $uibModal = _$uibModal_;
    modalInstance = $uibModalInstance.fake();

    spyOn($uibModal, 'open');
  }));

  it('should open help popup on click', function() {

    element = $compile('<button open-help="filters" editor-mode="page"></button>')(scope);
    scope.$digest();
    directiveScope = element.isolateScope();

    expect($uibModal.open).not.toHaveBeenCalled();

    element.click();

    expect($uibModal.open).toHaveBeenCalled();
    expect($uibModal.open.calls.mostRecent().args[0].templateUrl).toEqual('js/editor/header/help/help-popup.html');
    expect($uibModal.open.calls.mostRecent().args[0].size).toEqual('lg');
    expect(directiveScope.helpSection).toBe('filters');
    expect(directiveScope.editorMode).toBe('page');
  });

});
