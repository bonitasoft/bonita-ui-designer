describe('whiteboard component wrapper', function() {

  var service, item, widget, parentRow, whiteboardService;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function($injector) {
    service = $injector.get('whiteboardComponentWrapper');
    whiteboardService = $injector.get('whiteboardService');
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
      type: 'container'
    };

    service.wrapContainer(containerDefinition, item, parentRow);

    expect(item.$$id).toBe('container-0');
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
      id: 'formContainer',
      type: 'formContainer'
    };

    service.wrapFormContainer(formContainerDefinition, item, parentRow);

    expect(item.$$id).toBe('formContainer-0');
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
    item.tabs = [{
      title: 'tab1',
      container: {
        type: 'container',
        rows: [
          []
        ]
      }
    }, {
      title: 'tab2',
      container: {
        type: 'container',
        rows: [
          []
        ]
      }
    }];
    var tabsContainerDefinition = {
      id: 'tabsContainer',
      type: 'tabsContainer'
    };

    service.wrapTabsContainer(tabsContainerDefinition, item, parentRow);

    expect(item.$$id).toBe('tabsContainer-0');
    expect(item.$$widget).toEqual(tabsContainerDefinition);
    expect(item.$$widget).not.toBe(tabsContainerDefinition);
    expect(item.$$templateUrl).toBeDefined();
    expect(item.$$parentContainerRow).toBe(parentRow);
    expect(item.triggerRemoved).toBeDefined();
    expect(item.triggerAdded).toBeDefined();
    expect(service.wrapContainer.calls.count()).toBe(2);

    item.tabs.forEach(function(tab) {
      expect(tab.$$parentTabsContainer).toBe(item);
      expect(tab.$$widget.name).toBe('Tab');
      expect(tab.$$propertiesTemplateUrl).toBe('js/editor/properties-panel/tab-properties-template.html');
      expect(tab.triggerRemoved).toBeDefined();
      expect(tab.triggerAdded).toBeDefined();
    });
  });

  it('should initialize a tab', function() {
    var tab = { title: 'aTab' };
    var tabContainer = { type: 'tabsContainer' };

    service.wrapTab(tab, tabContainer);

    expect(tab.$$parentTabsContainer).toBe(tabContainer);
    expect(tab.$$widget.name).toBe('Tab');
    expect(tab.$$propertiesTemplateUrl).toBe('js/editor/properties-panel/tab-properties-template.html');
    expect(tab.triggerRemoved).toBeDefined();
    expect(tab.triggerAdded).toBeDefined();
  });

});
