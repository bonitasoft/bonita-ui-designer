import aWidget from  '../utils/builders/WidgetElementBuilder';

describe('EditorCtrl', function() {
  var $scope, pageRepo, $q, $location, $state, $window, tabsContainerStructureMockJSON, componentUtils, whiteboardService;

  beforeEach(angular.mock.module('bonitasoft.designer.editor', 'tabsContainerStructureMock'));

  beforeEach(inject(function($rootScope, $controller, $injector) {

    $window = {};
    $q = $injector.get('$q');
    $scope = $injector.get('$rootScope').$new();
    $location = $injector.get('$location');
    $state = $injector.get('$state');
    pageRepo = $injector.get('pageRepo');
    tabsContainerStructureMockJSON = $injector.get('tabsContainerStructureMockJSON');
    componentUtils = $injector.get('componentUtils');
    whiteboardService = $injector.get('whiteboardService');

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
          hello: { value: 4, type: 'constant' }
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
      spyOn(componentUtils.column, 'computeSizeItemInRow');
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
    $scope.selectComponent(tab, event);

    // then we should have an element selected
    expect($scope.currentContainerRow).toBeFalsy();
    expect($scope.currentComponent).toBe(tab);
    expect(event.stopPropagation).toHaveBeenCalled();
  });

  describe('Add a widget', function() {

    var widget, dragData, container;

    beforeEach(function() {
      // given a page with 1 row, which is the current row
      container = {
        rows: [
          []
        ]
      };
      widget = aWidget();
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

    it('should append a component to first row if page is empty', function() {
      $scope.page.rows = [[]];
      $scope.appendComponent(null, dragData);

      expect($scope.page.rows.length).toBe(1);

      var lastRow = $scope.page.rows.slice(-1)[0];
      expect(lastRow.length).toBe(1);
      expect($scope.currentComponent).toEqual(widget);
      expect(widget.triggerAdded).toHaveBeenCalled();
    });

    it('should append a component and create a new row at the end', function() {
      $scope.page.rows = [[{ 'name': 'titi' }]];
      var nbRow = $scope.page.rows.length;
      $scope.appendComponent(null, dragData);

      expect($scope.page.rows.length).toBe(nbRow + 1);

      var lastRow = $scope.page.rows.slice(-1)[0];
      expect(lastRow.length).toBe(1);
      expect($scope.currentComponent).toEqual(widget);
      expect(widget.triggerAdded).toHaveBeenCalled();
    });

    it('should add a component to a row', function() {
      expect(container.rows[0].length).toBe(0);
      $scope.addComponentToRow(dragData, container, container.rows[0], 0);

      expect(container.rows[0].length).toBe(1);
      expect($scope.currentComponent).toEqual(widget);
      expect(widget.triggerAdded).toHaveBeenCalled();
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
          xs: 12
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
    spyOn(whiteboardService, 'triggerRowRemoved');

    $scope.removeCurrentRow();

    expect(container.rows.length).toBe(1);
    expect(container.rows[0]).toBe(row2);
    expect($scope.currentContainerRow).toBeNull();
    expect(whiteboardService.triggerRowRemoved).toHaveBeenCalled();
  });

  it('should not remove row when there is only one in a container', function() {
    var row1 = [];

    var container = {
      rows: [row1]
    };
    $scope.currentContainerRow = {
      container: container,
      row: row1
    };
    spyOn(whiteboardService, 'triggerRowRemoved');

    $scope.removeCurrentRow();

    expect(container.rows.length).toBe(1);
    expect(container.rows[0]).toBe(row1);
    expect(whiteboardService.triggerRowRemoved).not.toHaveBeenCalled();
  });

  it('should remove the current component and trigger "removed" while removing it', function() {

    var container = {
      rows: [
        []
      ]
    };

    $scope.currentContainerRow = {
      container: container,
      row: container.rows[0]
    };

    var component = aWidget().withParentContainerRow($scope.currentContainerRow);

    var dragData = {
      create: function() {
        return component;
      }
    };

    $scope.addComponent(dragData, 0);

    $scope.removeCurrentComponent();

    expect(container.rows[0].length).toBe(0);
    expect($scope.currentComponent).toBeNull();
    expect(component.triggerRemoved).toHaveBeenCalled();
  });

  it('should not trigger "removed" while moving a component from a row to another', function() {

    var container = {
      rows: [
        []
      ]
    };

    $scope.currentContainerRow = {
      container: container,
      row: container.rows[0]
    };

    var component = aWidget().withParentContainerRow($scope.currentContainerRow);

    $scope.removeCurrentComponent(component, {});

    expect(container.rows[0].length).toBe(0);
    expect($scope.currentComponent).toBeNull();
    expect(component.triggerRemoved).not.toHaveBeenCalled();
  });

  it('should remove row when removing last component of a row', function() {

    var container = {
      rows: [
        [], []
      ]
    };

    $scope.currentContainerRow = {
      container: container,
      row: container.rows[1]
    };

    var dragData = {
      create: function() {
        return aWidget().withParentContainerRow($scope.currentContainerRow);
      }
    };

    $scope.addComponent(dragData, 0);
    var component = $scope.currentComponent;
    $scope.deselectComponent();

    $scope.removeCurrentComponent(component);

    expect(container.rows[1]).toBeUndefined();
    expect($scope.currentComponent).toBeNull();
  });

  it('should not remove row when moving component in the same row', function() {

    var container = {
      rows: [
        [], []
      ]
    };

    $scope.currentContainerRow = {
      container: container,
      row: container.rows[1]
    };

    var dragData = {
      create: function() {
        return aWidget().withParentContainerRow($scope.currentContainerRow);
      }
    };

    $scope.addComponent(dragData, 0);
    var component = $scope.currentComponent;

    $scope.removeCurrentComponent(component, component.$$parentContainerRow.row);

    expect(container.rows[1]).toEqual([]);
    expect($scope.currentComponent).toBeNull();
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

  it('should deselect a component', function() {
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

  xit('should save a page using CTRL+s', function() {
    spyOn(pageRepo, 'save');
    //$scope.save();
    // given a page
    $scope.page = { id: 'person' };

    document.addEventListener('keydown', function(e) {
      console.log(e.type, e.ctrlKey, e.shiftKey);
    });

    var event = document.createEvent('Event');
    event.initEvent('keydown', true, true);
    event.ctrlKey = true;
    event.shiftKey = true;
    event.keyCode = 'S'.charCodeAt(0);
    event.which = 's'.charCodeAt(0);
    document.dispatchEvent(event);

    expect(pageRepo.save).toHaveBeenCalledWith('person', $scope.page);
  });

  it('should save and edit a custom widget', function() {
    spyOn(pageRepo, 'save').and.callFake(function() {
      return $q.when({});
    });
    spyOn($state, 'go');

    // given a page
    $scope.page = { id: 'person' };

    // when we go to preview
    $scope.saveAndEditCustomWidget('widgetId');
    $scope.$apply();

    // then it should call the service to save
    expect(pageRepo.save).toHaveBeenCalledWith( $scope.page);
    // and set the path and search
    expect($state.go).toHaveBeenCalledWith('designer.widget', { id: 'widgetId' });
  });

  it('should check that a page can be saved', function() {
    expect($scope.canBeSaved({ name: '' })).toBeFalsy();
    expect($scope.canBeSaved({ name: 'pageName' })).toBeTruthy();
  });
});
