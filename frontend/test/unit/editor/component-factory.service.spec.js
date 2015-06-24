describe('componentFactory', function() {

  var service, item, widget, parentRow ;

  beforeEach(module('pb.services', 'pb.common.services'));
  beforeEach(module('pb.factories'));

  beforeEach(inject(function ($injector){
    service = $injector.get('componentFactory');
  }));

  beforeEach(function(){
    parentRow = {};
    item = {
      rows:[]
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

  it('should create a new widget', function() {
    spyOn(service, 'initializeWidget');

    var newCompo = service.createWidget(widget, parentRow);

    expect(newCompo.type).toBe('component');
    expect(newCompo.dimension.xs).toBe(12);
    expect(newCompo.propertyValues.robert.type).toBe('constant');
    expect(newCompo.propertyValues.robert.value).toBe('manger');
    expect(service.initializeWidget).toHaveBeenCalled();
  });

  it('should create a new container', function() {
    spyOn(service, 'initializeContainer');
    var newCompo = service.createContainer(parentRow);

    expect(newCompo.type).toBe('container');
    expect(newCompo.dimension.xs).toBe(12);
    expect(newCompo.propertyValues.hasOwnProperty('repeatedCollection')).toBe(true);
    expect(newCompo.propertyValues.hasOwnProperty('repeatedCollection')).toBe(true);
    expect(newCompo.rows.length).toBe(1);
    expect(service.initializeContainer).toHaveBeenCalled();
  });

  it('should create a new formContainer', function() {
    spyOn(service, 'initializeFormContainer');
    var newCompo = service.createFormContainer(parentRow);

    expect(newCompo.type).toBe('formContainer');
    expect(newCompo.dimension.xs).toBe(12);
    expect(newCompo.propertyValues.hasOwnProperty('url')).toBe(true);
    expect(newCompo.propertyValues.hasOwnProperty('method')).toBe(true);
    expect(newCompo.container).toBeDefined();
    expect(newCompo.container.rows.length).toBe(1);
    expect(service.initializeFormContainer).toHaveBeenCalled();
  });

  it('should create a new tabsContainer', function() {
    spyOn(service, 'initializeTabsContainer');
    var newCompo = service.createTabsContainer(parentRow);

    expect(newCompo.type).toBe('tabsContainer');
    expect(newCompo.dimension.xs).toBe(12);
    expect(newCompo.tabs.length).toBe(2);
    expect(service.initializeTabsContainer).toHaveBeenCalled();
  });

  it('should initialize a widget', function(){
    service.initializeWidget(widget, item, parentRow);
    expect(item.$$id).toBe('component-0');
    expect(item.$$widget).toBe(widget);
    expect(item.$$templateUrl).toBeDefined();
    expect(item.$$parentContainerRow).toBe(parentRow);
  });

  it('should init a container', function() {
    service.initializeContainer(item, parentRow);
    expect(item.$$id).toBe('container-0');
    expect(item.$$widget).toBeDefined();
    expect(item.$$templateUrl).toBeDefined();
    expect(item.$$parentContainerRow).toBe(parentRow);
  });

  it('should init a formContainer', function() {
    spyOn(service, 'initializeContainer');
    service.initializeFormContainer(item, parentRow);
    expect(item.$$id).toBe('formContainer-0');
    expect(item.$$widget).toBeDefined();
    expect(item.$$templateUrl).toBeDefined();
    expect(item.$$parentContainerRow).toBe(parentRow);
    expect(service.initializeContainer).toHaveBeenCalled();
  });

  it('should init a tabsContainer and its tabs', function() {
    spyOn(service, 'initializeContainer');
    item.tabs = ['tab1', 'tab2'].map(service.createNewTab);
    service.initializeTabsContainer(item, parentRow);
    expect(item.$$id).toBe('tabsContainer-0');
    expect(item.$$widget).toBeDefined();
    expect(item.$$templateUrl).toBeDefined();
    expect(item.$$parentContainerRow).toBe(parentRow);
    expect(service.initializeContainer.calls.count()).toBe(2);

    item.tabs.forEach(function(tab) {
      expect(tab.$$parentTabsContainer).toBe(item);
      expect(tab.$$widget.name).toBe('Tab');
      expect(tab.$$propertiesTemplateUrl).toBe('js/editor/properties-panel/tab-properties-template.html');
    });
  });

  it('should create a new tab', function() {
    var parentRow = {};
    var title = "tab";
    var tab = service.createNewTab(title);
    expect(tab.title).toEqual(title);
    expect(Object.keys(tab.container.propertyValues).length).toBeGreaterThan(0);
    expect(tab.container.rows.length).toBe(1);
  });

  it('should initialize a tab', function() {
    var tab = { title: 'aTab'};
    var tabContainer = { type: 'tabsContainer' };

    service.initializeTab(tab, tabContainer);

    expect(tab.$$parentTabsContainer).toBe(tabContainer);
    expect(tab.$$widget.name).toBe('Tab');
    expect(tab.$$propertiesTemplateUrl).toBe('js/editor/properties-panel/tab-properties-template.html');
  });

  describe('getNextId', function() {
    it('should return an id', function() {
      expect(service.getNextId('toto')).toEqual('toto-0');
    });

    it('should return the incremented id', function() {
      service.getNextId('toto')
      expect(service.getNextId('toto')).toEqual('toto-1');
    });
  });

});
