describe('Tab', () => {

  let $state, tab, isBottomPanelClosed, bottomPanel;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.bottom-panel'));
  beforeEach(inject((_$state_, tabFactory) => {
    $state = _$state_;
    spyOn($state, 'go');

    isBottomPanelClosed = false;
    bottomPanel = {
      open: jasmine.createSpy('open'),
      close: jasmine.createSpy('close'),
      isClosed: () => isBottomPanelClosed
    };

    tab = tabFactory.create({
      $state,
      name: 'Variables',
      stateName: 'designer.page.variables',
      bottomPanel
    });
  }));

  it('should be inactive when bottom panel is closed', () => {
    isBottomPanelClosed = true;

    expect(tab.isActive()).toBe(false);
  });

  it('should be inactive when on a different state than the tab one', () => {
    $state.current.name = 'designer.page.assets';

    expect(tab.isActive()).toBe(false);
  });

  it('should be active when current state is tab state', () => {
    $state.current.name = 'designer.page.variables';

    expect(tab.isActive()).toBe(true);
  });

  it('should close bottom panel when activated while already active', () => {
    $state.current.name = 'designer.page.variables';

    tab.activate();

    expect(bottomPanel.open).not.toHaveBeenCalled();
    expect(bottomPanel.close).toHaveBeenCalled();
  });

  it('should open bottom panel when activated while bottom panel is closed', () => {
    $state.current.name = 'designer.page.variables';
    isBottomPanelClosed = true;

    tab.activate();

    expect(bottomPanel.open).toHaveBeenCalled();
    expect(bottomPanel.close).not.toHaveBeenCalled();
  });

  it('should call tab state when activated', () => {

    tab.activate();

    expect($state.go).toHaveBeenCalledWith('designer.page.variables', undefined, { location: false });
  });
});
