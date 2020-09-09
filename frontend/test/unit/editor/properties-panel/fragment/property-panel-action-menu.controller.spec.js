describe('PropertyPanelActionMenuCtrl', function() {
  var controller, $scope, fragmentRepo, $q, $state;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.properties-panel','bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function($controller, $rootScope, _fragmentRepo_, whiteboardComponentWrapper, _$q_, _$state_) {
    $scope = $rootScope.$new();
    fragmentRepo = _fragmentRepo_;
    $q = _$q_;
    $state = _$state_;

    controller = $controller('PropertyPanelActionMenuCtrl', {
      $scope: $scope,
      $state: $state,
      fragmentRepo: _fragmentRepo_,
      whiteboardComponentWrapper: whiteboardComponentWrapper
    });
  }));

  it('container should have an action button with ability to save as fragment', function() {
    // given a container currently selected
    var container = {
      type : 'container',
      $$widget : {
        id : 'pbContainer',
        type : 'container',
        custom : false
      },
      rows: []
    };
    container.$$parentContainerRow = {
      row: [container]
    };
    $scope.currentComponent = container;

    // Check the action button and associated methods
    expect(controller.hasActionButton()).toBeTruthy();
    expect(controller.isContainer()).toBeTruthy();
    expect(controller.canBeSavedAsFragment()).toBeTruthy();
    expect(controller.isViewable()).toBeFalsy();
    expect(controller.isFragment()).toBeFalsy();
  });

  it('container with validation error should not be saved as fragment', function() {
    // given a container including a modal container
    var container = {
      type : 'container',
      $$widget : {
        id : 'pbContainer',
        type : 'container',
        custom : false
      },
      rows: [{
        $$id: 'pbModalContainer-4',
        $$parentContainerRow: {
          container: {
            type: 'container',
          }
        },
        type: 'modalContainer',
        id: 'pbModalContainer',
        $$widget : {
          id : 'pbModalContainer',
          type : 'container',
          custom : false
        },
        container: {
          $$id: 'pbContainer-4',
          rows: [[]]
        }
      }

      ]
    };

    $scope.currentComponent = container;

    // Check the container cannot be saved as fragment
    expect(controller.hasActionButton()).toBeFalsy();
  });

  it('fragment should have an action button with ability to edit', function() {
    // given a container currently selected
    var fragment = {
      type : 'fragment',
      $$widget : {
        id : 'fragmentId',
        type : 'fragment',
        custom : false
      },
      rows: []
    };
     $scope.currentComponent = fragment;

    // Check the action button and associated methods
    expect(controller.hasActionButton()).toBeTruthy();
    expect(controller.isFragmentEditable()).toBeTruthy();
    expect(controller.canBeSavedAsFragment()).toBeFalsy();
  });

  it('should save and edit a fragment', function() {
    $scope.save = function() {
      return $q.when({});
    };
    spyOn($scope, 'save').and.callThrough();
    spyOn($state, 'go');

    // given a page
    $scope.page = { id: 'person' };

    // when we go to preview
    controller.saveAndEditFragment('fragmentId');
    $scope.$apply();

    // then it should call the service to save
    expect($scope.save).toHaveBeenCalled();
    // and set the path and search
    expect($state.go).toHaveBeenCalledWith('designer.fragment', { id: 'fragmentId' });
  });


});
