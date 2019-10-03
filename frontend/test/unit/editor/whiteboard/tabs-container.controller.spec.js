import aTabContainer from '../../utils/builders/TabContainerElementBuilder';

describe('TabsContainerDirectiveCtrl', function() {
  var $scope;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));
  beforeEach(inject(function($rootScope, $controller, components) {
    $scope = $rootScope.$new();

    $scope.tabsContainer = {
      tabList: [
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

    spyOn(components, 'getById').and.returnValue({
      component: {
        id: 'pbTabContainer',
        properties: [
          {
            name: 'title',
            bond: 'constant',
            defaultValue: 'Tab X'
          }
        ]
      }
    });

  }));

  it('should have the first tab opened, but not selected in the editor', function() {
    expect($scope.isOpened($scope.tabsContainer.tabList[0])).toBe(true);
    expect($scope.isOpened($scope.tabsContainer.tabList[1])).toBe(false);
    expect($scope.editor.selectComponent).not.toHaveBeenCalled();
  });

  it('should open a tab and select it int he editor', function() {
    var event = {};
    $scope.openTab($scope.tabsContainer.tabList[1], event);
    expect($scope.isOpened($scope.tabsContainer.tabList[0])).toBe(false);
    expect($scope.isOpened($scope.tabsContainer.tabList[1])).toBe(true);
    expect($scope.editor.selectComponent).toHaveBeenCalledWith($scope.tabsContainer.tabList[1], event);
  });

  it('should be possible to move left only if current tab and not first tab', function() {
    spyOn($scope.editor, 'isCurrentComponent').and.returnValue(false);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabList[0])).toBe(false);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabList[1])).toBe(false);

    $scope.editor.isCurrentComponent.and.returnValue(true);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabList[0])).toBe(false);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabList[1])).toBe(true);
  });

  it('should be possible to move right only if current tab and not first tab', function() {
    spyOn($scope.editor, 'isCurrentComponent').and.returnValue(false);
    expect($scope.moveTabRightVisible($scope.tabsContainer.tabList[0])).toBe(false);
    expect($scope.moveTabRightVisible($scope.tabsContainer.tabList[1])).toBe(false);

    $scope.editor.isCurrentComponent.and.returnValue(true);
    expect($scope.moveTabRightVisible($scope.tabsContainer.tabList[0])).toBe(true);
    expect($scope.moveTabRightVisible($scope.tabsContainer.tabList[1])).toBe(false);
  });

  it('should move tab left', function() {
    var tab1 = $scope.tabsContainer.tabList[0];
    var tab2 = $scope.tabsContainer.tabList[1];
    $scope.moveTabLeft(tab2);
    expect($scope.tabsContainer.tabList[0]).toBe(tab2);
    expect($scope.tabsContainer.tabList[1]).toBe(tab1);
  });

  it('should move tab right', function() {
    $scope.moveTabRight($scope.tabsContainer.tabList[0]);
    expect($scope.tabsContainer.tabList[0].name).toBe('tab-1');
    expect($scope.tabsContainer.tabList[1].name).toBe('tab-0');

  });

  it('should add tab at the end', function() {
    $scope.tabsContainer = {
      tabList: [
        { name: 'tab-0',
          propertyValues: {title: {value: 'Tab 1'}}
        },
        { name: 'tab-1',
          propertyValues: {title: {value: 'Tab 2'}}
        }
      ]
    };

    $scope.addTabContainer();

    expect($scope.tabsContainer.tabList.length).toBe(3);
    expect($scope.tabsContainer.tabList[2].propertyValues.title.value).toBe('Tab 3');
    expect($scope.tabsContainer.tabList[2].container.rows).toEqual([[]]);
  });

  it('when a tab is added, should NOT generate twice the same tab title', function () {
    $scope.tabsContainer = {
      tabList: [
        {
          name: 'tab-0',
          propertyValues: {title: {value: 'Tab 2'}}
        }
      ]
    };
    $scope.addTabContainer();

    expect($scope.tabsContainer.tabList.length).toBe(2);
    expect($scope.tabsContainer.tabList[1].propertyValues.title.value).toBe('Tab 3');
  });

  it('should remove current tab and select previous one', function() {
    var toBeRemoved = aTabContainer().title('tab-2');
    var nextSelected = aTabContainer().title('tab-1');
    $scope.tabsContainer = {
      tabList: [
        nextSelected,
        toBeRemoved,
        aTabContainer().title('tab-3')
      ]
    };

    $scope.removeTab(toBeRemoved);

    expect($scope.tabsContainer.tabList.length).toBe(2);
    expect($scope.tabsContainer.$$openedTab).toEqual(nextSelected);
    expect($scope.editor.selectComponent).toHaveBeenCalledWith(nextSelected, undefined);
    expect(toBeRemoved.triggerRemoved).toHaveBeenCalled();
  });

  it('should remove current tab and select first when deleting first tab', function() {
    var toBeRemoved = aTabContainer().title('tab-1');
    var nextSelected = aTabContainer().title('tab-2');
    $scope.tabsContainer = {
      tabList: [
        toBeRemoved,
        nextSelected,
        aTabContainer().title('tab-3')
      ]
    };

    $scope.removeTab(toBeRemoved);

    expect($scope.tabsContainer.tabList.length).toBe(2);
    expect($scope.tabsContainer.$$openedTab).toEqual(nextSelected);
    expect($scope.editor.selectComponent).toHaveBeenCalledWith(nextSelected, undefined);
    expect(toBeRemoved.triggerRemoved).toHaveBeenCalled();
  });

  it('should hide remove button when there is only one tab', function() {
    $scope.tabsContainer = {
      tabList: [
        { name: 'tab-1' }
      ]
    };
    spyOn($scope.editor, 'isCurrentComponent').and.returnValue(true);

    var visible = $scope.isRemoveTabVisible($scope.tabsContainer.tabList[0]);

    expect(visible).toBeFalsy();
  });

  it('should show remove button when there is more than one tab', function() {
    $scope.tabsContainer = {
      tabList: [
        { name: 'tab-1' },
        { name: 'tab-2' }
      ]
    };
    spyOn($scope.editor, 'isCurrentComponent').and.returnValue(true);

    var visible = $scope.isRemoveTabVisible($scope.tabsContainer.tabList[0]);

    expect(visible).toBeTruthy();
  });
});
