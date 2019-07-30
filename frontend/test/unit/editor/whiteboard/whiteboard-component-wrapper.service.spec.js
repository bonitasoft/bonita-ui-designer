describe('whiteboard component wrapper', function() {

  var service, item, widget, parentRow, whiteboardService, components;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function($injector) {
    service = $injector.get('whiteboardComponentWrapper');
    whiteboardService = $injector.get('whiteboardService');
    components = $injector.get('components');
  }));

  beforeEach(function() {
    parentRow = {};
    item = {
      rows: []
    };
    widget = {
      id: 'pbJeanne',
      properties: [
        {
          name: 'robert',
          bond: 'constant',
          defaultValue: 'manger'
        }
      ]
    };
  });

  it('should initialize a widget', function() {
    spyOn(whiteboardService, 'triggerInitWidget');

    service.wrapWidget(widget, item, parentRow);

    expect(item.$$id).toBe('component-0');
    expect(item.$$widget).toEqual(widget);
    expect(item.$$templateUrl).toBeDefined();
    expect(item.$$parentContainerRow).toBe(parentRow);
    expect(item.triggerRemoved).toBeDefined();
    expect(item.triggerAdded).toBeDefined();
    expect(whiteboardService.triggerInitWidget).toHaveBeenCalled();
  });

  it('should init a container', function() {
    var containerDefinition = {
      id: 'pbContainer',
      type: 'container'
    };

    service.wrapContainer(containerDefinition, item, parentRow);

    expect(item.$$id).toBe('pbContainer-0');
    expect(item.$$widget).toEqual(containerDefinition);
    expect(item.$$widget).not.toBe(containerDefinition);
    expect(item.$$templateUrl).toBeDefined();
    expect(item.$$parentContainerRow).toBe(parentRow);
    expect(item.triggerRemoved).toBeDefined();
    expect(item.triggerAdded).toBeDefined();
  });

  it('should init a formContainer', function() {
    spyOn(service, 'wrapContainer');
    var formContainerDefinition = {
      id: 'pbFormContainer',
      type: 'formContainer'
    };

    service.wrapFormContainer(formContainerDefinition, item, parentRow);

    expect(item.$$id).toBe('pbFormContainer-0');
    expect(item.$$widget).toEqual(formContainerDefinition);
    expect(item.$$widget).not.toBe(formContainerDefinition);
    expect(item.$$templateUrl).toBeDefined();
    expect(item.$$parentContainerRow).toBe(parentRow);
    expect(item.triggerRemoved).toBeDefined();
    expect(item.triggerAdded).toBeDefined();
    expect(service.wrapContainer).toHaveBeenCalled();
  });

  it('should init a tabsContainer and its tabs', function() {
    spyOn(service, 'wrapContainer');
    spyOn(service, 'wrapTabContainer');
    spyOn(components, 'getById').and.returnValue({component: {}});
    item.tabList = [{
      title: 'tab1',
      container: {
        id: 'pbContainer',
        type: 'container',
        rows: [
          []
        ]
      }
    }, {
      title: 'tab2',
      container: {
        id: 'pbContainer',
        type: 'container',
        rows: [
          []
        ]
      }
    }];
    var tabsContainerDefinition = {
      id: 'pbTabsContainer',
      type: 'tabsContainer'
    };

    service.wrapTabsContainer(tabsContainerDefinition, item, parentRow);

    expect(item.$$id).toBe('pbTabsContainer-0');
    expect(item.$$widget).toEqual(tabsContainerDefinition);
    expect(item.$$widget).not.toBe(tabsContainerDefinition);
    expect(item.$$templateUrl).toBeDefined();
    expect(item.$$parentContainerRow).toBe(parentRow);
    expect(item.triggerRemoved).toBeDefined();
    expect(item.triggerAdded).toBeDefined();
    expect(service.wrapTabContainer.calls.count()).toBe(2);

  });

  it('should initialize a tab', function() {
    spyOn(service, 'wrapContainer');
    var tabContainerDefinition = {
      id: 'pbTabContainer',
      type: 'container'
    };

    service.wrapTabContainer(tabContainerDefinition, item, parentRow);

    expect(item.$$id).toBe('pbTabContainer-0');
    expect(item.$$widget).toEqual(tabContainerDefinition);
    expect(item.$$widget).not.toBe(tabContainerDefinition);
    expect(item.$$templateUrl).toBeDefined();
    expect(item.$$parentContainerRow).toBe(parentRow);
    expect(item.triggerRemoved).toBeDefined();
    expect(item.triggerAdded).toBeDefined();
  });

});
