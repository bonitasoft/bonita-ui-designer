import aTab from '../../utils/builders/TabElementBuilder';
import aFormContainer from '../../utils/builders/FormContainerElementBuilder';
import aTabsContainer from  '../../utils/builders/TabsContainerElementBuilder';
import aContainer from  '../../utils/builders/ContainerElementBuilder';
import aWidget from  '../../utils/builders/WidgetElementBuilder';

describe('whiteboard service', function() {

  var whiteboardService, $timeout;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function(_whiteboardService_, _$timeout_) {
    whiteboardService = _whiteboardService_;
    $timeout = _$timeout_;
  }));

  it('should add widget to whiteboard widget list while initializing a widget', function() {
    var aWidget = { id: 'aWidget' };
    var anotherWidget = { id: 'anotherWidget' };

    whiteboardService.triggerInitWidget(aWidget);

    expect(whiteboardService.contains(aWidget)).toBeTruthy();
    expect(whiteboardService.contains(anotherWidget)).toBeFalsy();
  });

  it('should remove widget from whiteboard widget list while removing a widget', function() {
    var aWidget = { id: 'aWidget' };
    var anotherWidget = { id: 'anotherWidget' };
    whiteboardService.triggerInitWidget(aWidget);
    whiteboardService.triggerInitWidget(anotherWidget);

    whiteboardService.onRemoveWidget(anotherWidget);

    expect(whiteboardService.contains(aWidget)).toBeTruthy();
    expect(whiteboardService.contains(anotherWidget)).toBeFalsy();
  });

  it('should not remove widget from whiteboard widget list while removing a widget but not last', function() {
    var aWidget = { id: 'aWidget' };
    whiteboardService.triggerInitWidget(aWidget);
    whiteboardService.triggerInitWidget(aWidget);

    whiteboardService.onRemoveWidget(aWidget);

    expect(whiteboardService.contains(aWidget)).toBeTruthy();
  });

  it('should call registered functions while removing a widget', () => {
    var fnOne = jasmine.createSpy('fnOne');
    var fnTwo = jasmine.createSpy('fnTwo');
    whiteboardService.registerOnWidgetRemoveFunction(fnOne);
    whiteboardService.registerOnWidgetRemoveFunction(fnTwo);
    var aWidget = { id: 'aWidget' };

    whiteboardService.onRemoveWidget(aWidget);

    // order matter, widget should be removed from list before function calls
    expect(whiteboardService.contains(aWidget)).toBeFalsy();
    $timeout.flush();
    expect(fnOne).toHaveBeenCalledWith(aWidget);
    expect(fnTwo).toHaveBeenCalledWith(aWidget);
  });

  it('should call registered functions while adding a widget', () => {
    var fnOne = jasmine.createSpy('fnOne');
    var fnTwo = jasmine.createSpy('fnTwo');
    whiteboardService.registerOnWidgetAddFunction(fnOne);
    whiteboardService.registerOnWidgetAddFunction(fnTwo);
    var aWidget = { id: 'aWidget' };

    whiteboardService.onAddWidget(aWidget);
    $timeout.flush();

    expect(fnOne).toHaveBeenCalledWith(aWidget);
    expect(fnTwo).toHaveBeenCalledWith(aWidget);
  });

  it('should trigger onRemove on row components while removing a row', () => {
    var aComponent = aWidget().id('aComponent');
    var anotherComponent = aWidget().id('anotherComponent');

    whiteboardService.triggerRowRemoved([aComponent, anotherComponent]);

    expect(aComponent.triggerRemoved).toHaveBeenCalled();
    expect(anotherComponent.triggerRemoved).toHaveBeenCalled();
  });

  it('should trigger onRemove on container components while removing a container', () => {
    var aComponent = aWidget().id('aComponent');
    var anotherComponent = aWidget().id('anotherComponent');
    var aComponentAgain = aWidget().id('aComponentAgain');
    var container = aContainer().withRow([aComponent, anotherComponent]).withRow([aComponentAgain]);

    whiteboardService.onRemoveContainer(container);

    expect(aComponent.triggerRemoved).toHaveBeenCalled();
    expect(anotherComponent.triggerRemoved).toHaveBeenCalled();
    expect(aComponentAgain.triggerRemoved).toHaveBeenCalled();
  });

  it('should trigger onRemove on tab components while removing a tab', () => {
    var aComponent = aWidget().id('aComponent');
    var anotherComponent = aWidget().id('anotherComponent');
    var aComponentAgain = aWidget().id('aComponentAgain');
    var tab = aTab().withRow([aComponent, anotherComponent]).withRow([aComponentAgain]);

    whiteboardService.onRemoveTab(tab);

    expect(aComponent.triggerRemoved).toHaveBeenCalled();
    expect(anotherComponent.triggerRemoved).toHaveBeenCalled();
    expect(aComponentAgain.triggerRemoved).toHaveBeenCalled();
  });

  it('should trigger onRemove on tabs containers components while removing a tabs container', () => {
    var aTab1Component = aWidget().id('aTab1Component');
    var anotherTab1Component = aWidget().id('anotherTab1Component');
    var aComponentTab1Again = aWidget().id('aComponentAgain');
    var tab2Component = aWidget().id('tab2Component');
    var tabsContainer = aTabsContainer()
      .withTab(aTab().withRow([aTab1Component, anotherTab1Component]).withRow([aComponentTab1Again]))
      .withTab(aTab().withRow([tab2Component]));

    whiteboardService.onRemoveTabsContainer(tabsContainer);

    expect(aTab1Component.triggerRemoved).toHaveBeenCalled();
    expect(anotherTab1Component.triggerRemoved).toHaveBeenCalled();
    expect(aComponentTab1Again.triggerRemoved).toHaveBeenCalled();
    expect(tab2Component.triggerRemoved).toHaveBeenCalled();
  });

  it('should trigger onRemove on form container components while removing a form container', () => {
    var aComponent = aWidget().id('aComponent');
    var anotherComponent = aWidget().id('anotherComponent');
    var aComponentAgain = aWidget().id('aComponentAgain');
    var formContainer = aFormContainer().withRow([aComponent]).withRow([anotherComponent, aComponentAgain]);

    whiteboardService.onRemoveFormContainer(formContainer);

    expect(aComponent.triggerRemoved).toHaveBeenCalled();
    expect(anotherComponent.triggerRemoved).toHaveBeenCalled();
    expect(aComponentAgain.triggerRemoved).toHaveBeenCalled();
  });

});
