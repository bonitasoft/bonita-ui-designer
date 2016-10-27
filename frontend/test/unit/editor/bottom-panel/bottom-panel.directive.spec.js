describe('Bottom panel', () => {

  let createBottomPanel, bottomPanel, clickOn;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.bottom-panel'));
  beforeEach(inject(($compile, $rootScope, $state) => {
    spyOn($state, 'go');
    $state.current.name = 'designer.page';
    createBottomPanel = mode => {
      let element = $compile(`<bottom-panel mode="${mode}"></bottom-panel>`)($rootScope);
      $rootScope.$apply();
      return element.find('.BottomPanel');
    };
    bottomPanel = createBottomPanel('page');
    clickOn = element => {
      element.click();
      $rootScope.$apply();
    };
  }));

  it('should have 2 tabs for a page', () => {
    expect(bottomPanel.find('.BottomPanel-tab').length).toBe(2);
  });

  it('should have 1 tabs for a fragment', () => {
    let fragmentBottomPanel = createBottomPanel('fragment');

    expect(fragmentBottomPanel.find('.BottomPanel-tab').length).toBe(1);
  });

  it('should close bottom panel', () => {
    clickOn(bottomPanel.find('.BottomPanel-tab--active'));

    expect(bottomPanel.hasClass('BottomPanel--closed')).toBe(true);
  });

  it('should open bottom panel', () => {
    clickOn(bottomPanel.find('.BottomPanel-tab--active'));

    clickOn(bottomPanel.find('.BottomPanel-tab'));

    expect(bottomPanel.hasClass('BottomPanel--closed')).toBe(false);
  });
});
