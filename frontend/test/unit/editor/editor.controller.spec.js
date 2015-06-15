
describe('EditorCtrl', function() {
  var $scope, pageRepo, $q, $location, $state, $window, tabsContainerStructureMockJSON;

  beforeEach(module('ui.bootstrap'));
  beforeEach(module('pb.controllers', 'pb.common.repositories'));
  beforeEach(module('pb.common.services'));
  beforeEach(module('pb.factories'));
  beforeEach(module('tabsContainerStructureMock'));
  beforeEach(module('ui.router'));

  beforeEach(inject(function($rootScope, $controller, $injector) {

    $window = {};
    $q = $injector.get('$q');
    $scope = $injector.get('$rootScope').$new();
    $location = $injector.get('$location');
    $state = $injector.get('$state');
    pageRepo = $injector.get('pageRepo');
    tabsContainerStructureMockJSON = $injector.get('tabsContainerStructureMockJSON');
    componentUtils = $injector.get('componentUtils');

    $controller('EditorCtrl', {
      $scope: $scope,
      $window: $window,
      selectedResolution: {},
      artifactRepo: pageRepo,
      artifact: {
        rows: [
          []
        ],
        data: {
          hello: {value: 4, type: 'constant'}
        }
      },
      mode: 'page'
    });
  }));


  it('should get the right element classes', function() {
    var component = {
      dimension: {
        xs: 6
      }
    };

    expect($scope.componentClasses(component)).toContain('col-xs-6');
    expect($scope.componentClasses(component).length).toBe(1);
  });

  it('should select the current row, and unselect the current component', function() {
    // given a page with 2 rows
    var container = {
      rows: [
        [
          {
            $$widget: {
              name: 'label'
            }
          }
        ],
        [
          {
            $$widget: {
              name: 'input'
            }
          }
        ]
      ]
    };
    $scope.currentComponent = container.rows[0][0];

    // when we select the second row
    var row = container.rows[1];
    var event = {
      stopPropagation: function() {
      }
    };
    spyOn(event, 'stopPropagation');
    $scope.selectRow(container, row, event);

    // then we should have a row selected
    expect($scope.currentContainerRow.container).toBe(container);
    expect($scope.currentContainerRow.row).toBe(row);
    expect($scope.currentComponent).toBeFalsy();
    expect(event.stopPropagation).toHaveBeenCalled();
  });

  describe('On drag and drop', function() {

    var element = {
      $$parentContainerRow: null,
      type: 'blank',
      dimension: {
        'xs': 12
      }
    };

    beforeEach(function() {
      spyOn(componentUtils.column, 'computeSizeItemInRow')
      $scope.currentContainerRow = {
        container: 'toto',
        row: [{
          type: 'blank',
          dimension: {
            'xs': 12
          }
        }]
      };
    });

    it('should recalculate the components dimensions', function() {
      $scope.dropElement(element);
      expect(componentUtils.column.computeSizeItemInRow).toHaveBeenCalledWith($scope.currentContainerRow.row);
    });

    it('should push the element to the current row', function() {
      $scope.dropElement(element);
      expect($scope.currentContainerRow.row.length).toBe(2);
    });

    it('should push the element to the current row', function() {
      $scope.dropElement(element);
      expect($scope.currentContainerRow.row.length).toBe(2);
    });
  });


  it('should select the component, and unselect the current row', function() {
    // given a page with 2 rows
    var container = {
      rows: [
        [
          {
            $$widget: {
              name: 'label'
            }
          }
        ],
        [
          {
            $$widget: {
              name: 'input'
            }
          }
        ]
      ]
    };
    $scope.currentContainerRow = {
      container: container,
      row: container.rows[0]
    };

    // when we select a component
    var component = container.rows[1][0];
    var event = {
      stopPropagation: function() {
      }
    };
    spyOn(event, 'stopPropagation');
    $scope.selectComponent(component, event);

    // then we should have an element selected
    expect($scope.currentContainerRow).toBeFalsy();
    expect($scope.currentComponent).toBe(component);
    expect(event.stopPropagation).toHaveBeenCalled();
  });

  it('should select the tab, and unselect the current row', function() {
    // given a page with a tabs container and 2 tabs
    var container = {
      counters: {
        tabsContainer: 0
      },
      rows: [
        [
          {
            type: 'tabsContainer',
            tabs: [
              {
                title: 'hello'
              },
              {
                title: 'world'
              }
            ]
          }
        ]
      ]
    };
    $scope.currentContainerRow = {
      container: container,
      row: container.rows[0]
    };

    // when we select a tab
    var tab = container.rows[0][0].tabs[1];
    var event = {
      stopPropagation: function() {
      }
    };
    spyOn(event, 'stopPropagation');
    $scope.selectTab(tab, event);

    // then we should have an element selected
    expect($scope.currentContainerRow).toBeFalsy();
    expect($scope.currentComponent).toBe(tab);
    expect(event.stopPropagation).toHaveBeenCalled();
  });

  describe('Add a widget', function() {

    var widget, dragData, row, container;

    beforeEach(function() {
      // given a page with 1 row, which is the current row
      container = {
        rows: [
          []
        ]
      };
      widget = {
            item: 'foo',
            dimension: {
              xs: 12
            }
          };
      dragData = {
        create: function() {
          return angular.copy(widget);
        }
      };

      $scope.currentContainerRow = {
        container: container,
        row: container.rows[0]
      };

      $scope.$apply();
    });

    it('should append a component to first row if page is empty', function(){
      $scope.page.rows = [[]];
      $scope.appendComponent(null, dragData);

      expect($scope.page.rows.length).toBe(1);

      var lastRow = $scope.page.rows.slice(-1)[0];
      expect(lastRow.length).toBe(1);
      expect($scope.currentComponent).toEqual(widget);
    });

    it('should append a component and create a new row at the end', function(){
      $scope.page.rows = [[{"name": "titi"}]];
      var nbRow = $scope.page.rows.length;
      $scope.appendComponent(null, dragData);

      expect($scope.page.rows.length).toBe(nbRow + 1);

      var lastRow = $scope.page.rows.slice(-1)[0];
      expect(lastRow.length).toBe(1);
      expect($scope.currentComponent).toEqual(widget);
    });

    it('should add a component to a row', function(){
      expect(container.rows[0].length).toBe(0);
      $scope.addComponentToRow(dragData, container, container.rows[0], 0);

      expect(container.rows[0].length).toBe(1);
      expect($scope.currentComponent).toEqual(widget);
    });

    it('it should resize the last widget of a row if row is not full', function() {
      container.rows[0][0] = {
        item: 'foo',
        dimension: {
          xs: 8
        }
      };
      $scope.addComponent(dragData, 1);
      expect(container.rows[0][1].dimension.xs).toBe(4);
    });

    it('resize all the widgets in a row after added a widget to it', function() {
      container.rows[0][0] = {
        item: 'foo',
        dimension: {
          xs: 12,
        }
      };
      $scope.addComponent(dragData, 1);
      expect(container.rows[0][0].dimension.xs).toBe(6);
      expect(container.rows[0][1].dimension.xs).toBe(6);
    });


    it('should add a widget and do not change the length if we already have 12 col', function() {
      $scope.addComponent(dragData,  0);
      expect(container.rows[0][0].dimension.xs).toBe(12);
    });

  });


  it('should add row before current', function() {
    var row1 = [];
    var row2 = [];

    var container = {
      rows: [row1, row2]
    };

    $scope.currentContainerRow = {
      container: container,
      row: row2
    };

    $scope.addRowBeforeCurrent();
    expect(container.rows.length).toBe(3);
    expect(container.rows[1]).toEqual([]);
    expect(container.rows[1]).not.toBe(row1);
    expect(container.rows[1]).not.toBe(row2);
    expect(container.rows[2]).toBe(row2);
  });

  it('should add row after current', function() {
    var row1 = [];
    var row2 = [];

    var container = {
      rows: [row1, row2]
    };
    $scope.currentContainerRow = {
      container: container,
      row: row2
    };

    $scope.addRowAfterCurrent();
    expect(container.rows.length).toBe(3);
    expect(container.rows[2]).toEqual([]);
    expect(container.rows[2]).not.toBe(row1);
    expect(container.rows[2]).not.toBe(row2);
    expect(container.rows[1]).toBe(row2);
  });

  it('should remove the current row', function() {
    var row1 = [];
    var row2 = [];

    var container = {
      rows: [row1, row2]
    };
    $scope.currentContainerRow = {
      container: container,
      row: row1
    };

    $scope.removeCurrentRow();

    expect(container.rows.length).toBe(1);
    expect(container.rows[0]).toBe(row2);
    expect($scope.currentContainerRow).toBeNull();
  });

  it('should remove the current component', function() {

    var container = {
      rows: [
        []
      ]
    };

    $scope.currentContainerRow = {
      container: container,
      row: container.rows[0]
    };

    var dragData = {
      create: function() {
        return {
          item: 'foo',
          $$parentContainerRow: $scope.currentContainerRow,
          dimension: {
            xs: 12
          }
        };
      }
    };

    $scope.addComponent(dragData, 0);

    $scope.removeCurrentComponent();

    expect(container.rows[0].length).toBe(0);
    expect($scope.currentComponent).toBeNull();
  });

  it('should add tab before current', function() {
    var tabsContainer = {
      tabs: []
    };
    var tab1 = {
      title: 'Tab 1',
      $$parentTabsContainer: tabsContainer
    };
    tabsContainer.tabs.push(tab1);

    $scope.currentComponent = tab1;

    $scope.addTabBeforeCurrent();
    expect(tabsContainer.tabs.length).toBe(2);
    expect(tabsContainer.tabs[0].title).toBe('Tab 2');
    expect(tabsContainer.tabs[0].$$parentTabsContainer).toBe(tabsContainer);
    expect(tabsContainer.tabs[0].container.rows).toEqual([
      []
    ]);
  });

  it('should add tab after current', function() {
    var tabsContainer = {
      tabs: []
    };
    var tab1 = {
      title: 'Tab 1',
      $$parentTabsContainer: tabsContainer
    };
    tabsContainer.tabs.push(tab1);

    $scope.currentComponent = tab1;

    $scope.addTabAfterCurrent();

    expect(tabsContainer.tabs.length).toBe(2);
    expect(tabsContainer.tabs[1].title).toBe('Tab 2');
    expect(tabsContainer.tabs[1].$$parentTabsContainer).toBe(tabsContainer);
    expect(tabsContainer.tabs[1].container.rows).toEqual([
      []
    ]);
  });

  it('should remove current tab', function() {
    var tabsContainer = {
      tabs: []
    };
    var tab1 = {
      title: 'Tab 1',
      $$parentTabsContainer: tabsContainer
    };
    var tab2 = {
      title: 'Tab 2',
      $$parentTabsContainer: tabsContainer
    };
    tabsContainer.tabs.push(tab1);
    tabsContainer.tabs.push(tab2);

    $scope.currentComponent = tab2;

    $scope.removeCurrentTab();

    expect(tabsContainer.tabs.length).toBe(1);
    expect($scope.currentComponent).toBeFalsy();
    expect(tabsContainer.$$openedTab).toBe(tab1);
  });

  it('should tell if a row is the current row', function() {
    var row1 = [];
    var row2 = [];
    var container = {
      rows: [row1, row2]
    };
    $scope.currentContainerRow = {
      container: container,
      row: row1
    };

    expect($scope.isCurrentRow(row1)).toBe(true);
    expect($scope.isCurrentRow(row2)).toBe(false);
  });

  it('should tell if a tab is the current tab', function() {

    $scope.currentComponent = {};

    expect($scope.isCurrentTab($scope.currentComponent)).toBe(true);
    expect($scope.isCurrentTab({})).toBe(false);
  });

  it('should compute row size', function() {
    // given a row with 2 components
    var row = [
      {
        dimension: {
          xs: 8
        }
      },
      {
        dimension: {
          xs: 2
        }
      }
    ];

    expect($scope.rowSize(row)).toBe(10);
  });

  it('should check if it is the current component', function() {
    // given a component
    var component = {
      dimension: {
        xs: 8,
        md: 4
      }
    };

    // when we check if it is the current one
    // then it should not be
    expect($scope.isCurrentComponent(component)).toBe(false);

    // when we make the component as the current one
    $scope.currentComponent = component;

    // when we check if it is the current one
    // then it should be
    expect($scope.isCurrentComponent(component)).toBe(true);
  });

  it('should deselect a component', function(){
    // given a component
    var component = {
      dimension: {
        xs: 8,
        md: 4
      }
    };
    // when we make the component as the current one
    $scope.currentComponent = component;
    // when we check if it is the current one
    // then it should not be
    expect($scope.isCurrentComponent(component)).toBe(true);
    $scope.editor.deselectComponent();
    expect($scope.isCurrentComponent(component)).toBe(false);
  });

  it('should save a page', function() {
    spyOn(pageRepo, 'save');

    // given a page
    $scope.page = {id: 'person'};

    // when we save
    $scope.save();

    // then it should call the service
    expect(pageRepo.save).toHaveBeenCalledWith('person', $scope.page);
  });

  xit('should save a page using CTRL+s', function() {
    spyOn(pageRepo, 'save');
    //$scope.save();
    // given a page
    $scope.page = {id: 'person'};

    document.addEventListener('keydown', function(e){
      console.log(e.type, e.ctrlKey, e.shiftKey)
    })


    var event = document.createEvent("Event");
    event.initEvent('keydown', true, true);
    event.ctrlKey = true;
    event.shiftKey = true;
    event.keyCode = "S".charCodeAt(0);
    event.which = "s".charCodeAt(0);
    document.dispatchEvent(event);

    expect(pageRepo.save).toHaveBeenCalledWith('person', $scope.page);
  });

  it('should save and export page', function() {
    spyOn(pageRepo, 'save').and.callFake(function() {
      return $q.when({});
    });

    // given a page
    $scope.page = {id: 'person'};

    // when we go to preview
    $scope.saveAndExport('preview');
    $scope.$apply();

    // then it should call the service to save
    expect(pageRepo.save).toHaveBeenCalledWith('person', $scope.page);
    // and set the path and search
    expect($window.location).toBe('export/page/person');
  });

  it('should save and edit a custom widget', function() {
    spyOn(pageRepo, 'save').and.callFake(function() {
      return $q.when({});
    });
    spyOn($state, 'go');

    // given a page
    $scope.page = {id: 'person'};

    // when we go to preview
    $scope.saveAndEditCustomWidget('widgetId');
    $scope.$apply();

    // then it should call the service to save
    expect(pageRepo.save).toHaveBeenCalledWith('person', $scope.page);
    // and set the path and search
    expect($state.go).toHaveBeenCalledWith('designer.widget', {widgetId: 'widgetId'});
  });
});
