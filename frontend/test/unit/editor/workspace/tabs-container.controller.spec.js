describe('TabsContainerDirectiveCtrl', function() {
  var $scope;

  beforeEach(module('pb.directives', 'pb.common.services'));
  beforeEach(inject(function($rootScope, $controller) {
    $scope = $rootScope.$new();

    $scope.tabsContainer = {
      tabs: [
        {name: 'tab-0'},
        {name: 'tab-1'}
      ]
    };
    $scope.editor = {
      selectTab: function() {},
      isCurrentTab: function() {}
    };
    spyOn($scope.editor, 'selectTab');
    $controller('TabsContainerDirectiveCtrl', {
      $scope: $scope
    });
  }));

  it('should have the first tab opened, but not selected in the editor', function() {
    expect($scope.isOpened($scope.tabsContainer.tabs[0])).toBe(true);
    expect($scope.isOpened($scope.tabsContainer.tabs[1])).toBe(false);
    expect($scope.editor.selectTab).not.toHaveBeenCalled();
  });

  it('should open a tab and select it int he editor', function() {
    var event = {};
    $scope.openTab($scope.tabsContainer.tabs[1], event);
    expect($scope.isOpened($scope.tabsContainer.tabs[0])).toBe(false);
    expect($scope.isOpened($scope.tabsContainer.tabs[1])).toBe(true);
    expect($scope.editor.selectTab).toHaveBeenCalledWith($scope.tabsContainer.tabs[1], event);
  });

  it('should be possible to move left only if current tab and not first tab', function() {
    spyOn($scope.editor, 'isCurrentTab').and.returnValue(false);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabs[0])).toBe(false);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabs[1])).toBe(false);

    $scope.editor.isCurrentTab.and.returnValue(true);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabs[0])).toBe(false);
    expect($scope.moveTabLeftVisible($scope.tabsContainer.tabs[1])).toBe(true);
  });

  it('should be possible to move right only if current tab and not first tab', function() {
    spyOn($scope.editor, 'isCurrentTab').and.returnValue(false);
    expect($scope.moveTabRightVisible($scope.tabsContainer.tabs[0])).toBe(false);
    expect($scope.moveTabRightVisible($scope.tabsContainer.tabs[1])).toBe(false);

    $scope.editor.isCurrentTab.and.returnValue(true);
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
});
