import aTab from '../../utils/builders/TabElementBuilder';

describe('TabsContainerDirectiveCtrl', function() {
  var $scope;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));
  beforeEach(inject(function($rootScope, $controller) {
    $scope = $rootScope.$new();

    $scope.tabsContainer = {
      tabs: [
        { name: 'tab-0' },
        { name: 'tab-1' }
      ]
    };
    $scope.editor = {
      selectComponent: function() {},
      isCurrentComponent: function() {}
    };
    spyOn($scope.editor, 'selectComponent');
    $controller('TabsContainerDirectiveCtrl', {
      $scope: $scope
    });
  }));

  it('should have the first tab opened, but not selected in the editor', function() {
    expect($scope.isOpened($scope.tabsContainer.tabs[0])).toBe(true);
    expect($scope.isOpened($scope.tabsContainer.tabs[1])).toBe(false);
    expect($scope.editor.selectComponent).not.toHaveBeenCalled();
  });

  it('should open a tab and select it int he editor', function() {
    var event = {};
    $scope.openTab($scope.tabsContainer.tabs[1], event);
    expect($scope.isOpened($scope.tabsContainer.tabs[0])).toBe(false);
    expect($scope.isOpened($scope.tabsContainer.tabs[1])).toBe(true);
    expect($scope.editor.selectComponent).toHaveBeenCalledWith($scope.tabsContainer.tabs[1], event);
  });

  it('should be possible to move left only if current tab and not first tab', function() {
    spyOn($scope.editor, 'isCurrentComponent').and.returnValue(false);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabs[0])).toBe(false);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabs[1])).toBe(false);

    $scope.editor.isCurrentComponent.and.returnValue(true);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabs[0])).toBe(false);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabs[1])).toBe(true);
  });

  it('should be possible to move right only if current tab and not first tab', function() {
    spyOn($scope.editor, 'isCurrentComponent').and.returnValue(false);
    expect($scope.moveTabRightVisible($scope.tabsContainer.tabs[0])).toBe(false);
    expect($scope.moveTabRightVisible($scope.tabsContainer.tabs[1])).toBe(false);

    $scope.editor.isCurrentComponent.and.returnValue(true);
    expect($scope.moveTabRightVisible($scope.tabsContainer.tabs[0])).toBe(true);
    expect($scope.moveTabRightVisible($scope.tabsContainer.tabs[1])).toBe(false);
  });

  it('should move tab left', function() {
    var tab1 = $scope.tabsContainer.tabs[0];
    var tab2 = $scope.tabsContainer.tabs[1];
    $scope.moveTabLeft(tab2);
    expect($scope.tabsContainer.tabs[0]).toBe(tab2);
    expect($scope.tabsContainer.tabs[1]).toBe(tab1);
  });

  it('should move tab right', function() {
    $scope.moveTabRight($scope.tabsContainer.tabs[0]);
    expect($scope.tabsContainer.tabs[0].name).toBe('tab-1');
    expect($scope.tabsContainer.tabs[1].name).toBe('tab-0');

  });

  it('should add tab at the end', function() {
    $scope.tabsContainer = {
      tabs: [
        { name: 'tab-0' },
        { name: 'tab-1' }
      ]
    };

    $scope.addTab();

    expect($scope.tabsContainer.tabs.length).toBe(3);
    expect($scope.tabsContainer.tabs[2].title).toBe('Tab 3');
    expect($scope.tabsContainer.tabs[2].$$parentTabsContainer).toBe($scope.tabsContainer);
    expect($scope.tabsContainer.tabs[2].container.rows).toEqual([[]]);
  });

  it('should remove current tab and select previous one', function() {
    var toBeRemoved = aTab().title('tab-2');
    var nextSelected = aTab().title('tab-1');
    $scope.tabsContainer = {
      tabs: [
        nextSelected,
        toBeRemoved,
        aTab().title('tab-3')
      ]
    };

    $scope.removeTab(toBeRemoved);

    expect($scope.tabsContainer.tabs.length).toBe(2);
    expect($scope.tabsContainer.$$openedTab).toEqual(nextSelected);
    expect($scope.editor.selectComponent).toHaveBeenCalledWith(nextSelected, undefined);
    expect(toBeRemoved.triggerRemoved).toHaveBeenCalled();
  });

  it('should remove current tab and select first when deleting first tab', function() {
    var toBeRemoved = aTab().title('tab-1');
    var nextSelected = aTab().title('tab-2');
    $scope.tabsContainer = {
      tabs: [
        toBeRemoved,
        nextSelected,
        aTab().title('tab-3')
      ]
    };

    $scope.removeTab(toBeRemoved);

    expect($scope.tabsContainer.tabs.length).toBe(2);
    expect($scope.tabsContainer.$$openedTab).toEqual(nextSelected);
    expect($scope.editor.selectComponent).toHaveBeenCalledWith(nextSelected, undefined);
    expect(toBeRemoved.triggerRemoved).toHaveBeenCalled();
  });

  it('should hide remove button when there is only one tab', function() {
    $scope.tabsContainer = {
      tabs: [
        { name: 'tab-1' }
      ]
    };
    spyOn($scope.editor, 'isCurrentComponent').and.returnValue(true);

    var visible = $scope.isRemoveTabVisible($scope.tabsContainer.tabs[0]);

    expect(visible).toBeFalsy();
  });

  it('should show remove button when there is more than one tab', function() {
    $scope.tabsContainer = {
      tabs: [
        { name: 'tab-1' },
        { name: 'tab-2' }
      ]
    };
    spyOn($scope.editor, 'isCurrentComponent').and.returnValue(true);

    var visible = $scope.isRemoveTabVisible($scope.tabsContainer.tabs[0]);

    expect(visible).toBeTruthy();
  });
});
