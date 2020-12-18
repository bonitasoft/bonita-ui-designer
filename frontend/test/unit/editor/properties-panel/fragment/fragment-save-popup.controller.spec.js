describe('SaveFragmentController', function () {
  let saveFragmentController, container, $scope, fragmentRepo, $q, currentComponent;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.properties-panel','bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function ($controller, $rootScope, _fragmentRepo_, _$q_) {
    $scope = $rootScope.$new();
    fragmentRepo = _fragmentRepo_;
    $q = _$q_;

    container = {
      rows: []
    };
    container.$$parentContainerRow = {
      row: [container]
    };

    currentComponent = container;
    var editor = {};
    editor.selectComponent = function () {
    };
    spyOn(editor, 'selectComponent');

    var $uibModalInstance = {};


    saveFragmentController = $controller('SaveFragmentController', {
      $scope: $scope,
      currentComponent: currentComponent,
      editor: editor,
      $uibModalInstance: $uibModalInstance,
      fragmentRepo: _fragmentRepo_,
    });
  }));

  it('should save container as a fragment', function () {
    spyOn(fragmentRepo, 'create').and.returnValue(
      $q.when({
        id: 'fragment-2',
        name: 'name',
        rows: []
      }));

    // when we save it
    saveFragmentController.saveAsFragment('name');
    $scope.$apply();

    // then we should have call the fragment repo
    var expectedFragment = {
      name: 'name',
      rows: container.rows
    };

    expect(fragmentRepo.create).toHaveBeenCalledWith(expectedFragment);
  });

});
