describe('ComponentUtils Service', function() {

  'use strict';

  var service, tabsContainerStructureMockJSON, pageJson, resolutions, $stateParams;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard', 'tabsContainerStructureMock', 'pageDataMock'));

  beforeEach(inject(function ($injector) {
    service = $injector.get('componentUtils');
    resolutions = $injector.get('resolutions');
    $stateParams = $injector.get('$stateParams');
    tabsContainerStructureMockJSON = $injector.get('tabsContainerStructureMockJSON');
    pageJson = $injector.get('pageJson');
    resolutions.registerResolutions([
      {
        key: 'xs',
        icon: 'laptop',
        width: 320
      },
      {
        key: 'foo',
        label: 'bar',
        icon: 'quux',
        width: 1337,
        tooltip: 'foo bar quux'
      }
    ]);
    resolutions.setDefaultResolution('foo');
  }));

  describe('test if we are the child of a container', function () {
    it('should not break if the item is not a tabContainer nor a container and return false', function () {
      expect(service.isChildOf('tab-1', {})).toBe(false);
    });

    it('should not break if the item is a container without rows and return false', function () {
      expect(service.isChildOf('tab-1', {type: 'container'})).toBe(false);
      expect(service.isChildOf('tab-1', {type: 'container', rows: []})).toBe(false);
    });

    it('should return false if a rows is undefined or a falsy value', function () {
      var item = {
        type: 'container',
        rows: [[{}], [0]]
      };
      expect(service.isChildOf('tab-1', item)).toBe(false);
    });

    it('should return true if we have a tab child of another one', function () {
      expect(service.isChildOf('tab-1', tabsContainerStructureMockJSON)).toBe(true);
      expect(service.isChildOf('tab-8', tabsContainerStructureMockJSON)).toBe(true);
      expect(service.isChildOf('tab-9', tabsContainerStructureMockJSON)).toBe(true);
      expect(service.isChildOf('tab-3', tabsContainerStructureMockJSON)).toBe(true);
      expect(service.isChildOf('tab-7', tabsContainerStructureMockJSON)).toBe(true);
    });

    it('should return true if we find a container and it is the one', function () {
      expect(service.isChildOf(150, tabsContainerStructureMockJSON)).toBe(true);
      expect(service.isChildOf(1421, tabsContainerStructureMockJSON)).toBe(true);
    });

    it('should return false if the tab is not a child', function () {
      expect(service.isChildOf('tab-25', tabsContainerStructureMockJSON)).toBe(false);
      expect(service.isChildOf(32, tabsContainerStructureMockJSON)).toBe(false);
    });
  });

  describe('modal containers test', function () {
    let modal, modalWithModal, containerWithModal, modalWithContainerWithModal,tabsContainerWithContainerWithModal;

    beforeEach(function () {
      modal = {
        $$id: 'pbModalContainer-1',
        type: 'modalContainer',
        id: 'pbModalContainer',
        container: {
          $$id: 'pbContainer-1',
          rows: [[{}]]
        }
      };

      modalWithModal = {
        $$id: 'pbModalContainer-3',
        type: 'modalContainer',
        id: 'pbModalContainer',
        container: {
          $$id: 'pbContainer-3',
          rows: [[
            modal
          ]]
        }
      };

      containerWithModal = {
        type: 'container',
        id: 'pbContainer',
        $$id: 'pbContainer-4',
        rows: [[
          modal
        ]]
      };

      modalWithContainerWithModal = {
        $$id: 'pbModalContainer-5',
        type: 'modalContainer',
        id: 'pbModalContainer',
        container: {
          $$id: 'pbContainer-5',
          rows: [[
            containerWithModal
          ]]
        }
      };

      tabsContainerWithContainerWithModal = {
        $$id: 'pbTabsContainer-8',
        $$parentContainerRow: {
          container: {
            type: 'page',
          }
        },
        type: 'tabsContainer',
        id: 'pbTabsContainer',
        tabList: [
          {
            type: 'tabContainer',
            id: 'pbTabContainer',
            container: {
              $$id: 'pbContainer-9',
              id: 'pbContainer',
              type: 'container',
              rows: [[
                containerWithModal
              ]]
            }
          }
        ]
      };
    });

    it('should return true if a modal container is our child', function () {
      expect(service.isChildOf('pbContainer-1', modal)).toBe(true);
      expect(service.isChildOf('pbContainer-1', modalWithModal)).toBe(true);
      expect(service.isChildOf('pbContainer-1', containerWithModal)).toBe(true);
      expect(service.isChildOf('pbContainer-1', modalWithContainerWithModal)).toBe(true);
      expect(service.isChildOf('pbContainer-1', tabsContainerWithContainerWithModal)).toBe(true);
    });

    it('should return false if a modal container is not our child', function () {
      expect(service.isChildOf('pbContainer-99', modal)).toBe(false);
      expect(service.isChildOf('pbContainer-99', containerWithModal)).toBe(false);
      expect(service.isChildOf('pbContainer-99', tabsContainerWithContainerWithModal)).toBe(false);
    });
  });

  describe('test if container contains a modal child ', function () {
    it('should return false if many modal containers are at the root af a page', function () {
      var modalAtRootOfPage = {
        type: 'page',
        name: 'page1',
        id: 'page-id-1',
        hasValidationError: false,
        rows: [[
          {
            $$id: 'pbModalContainer-1',
            $$parentContainerRow: {
              container: {
                type: 'page',
              }
            },
            type: 'modalContainer',
            id: 'pbModalContainer',
            container: {
              $$id: 'pbModalContainer-1',
              rows: [[]]
            }
          },
          {
            $$id: 'pbModalContainer-2',
            $$parentContainerRow: {
              container: {
                type: 'page',
              }
            },
            type: 'modalContainer',
            id: 'pbModalContainer',
            container: {
              $$id: 'pbModalContainer-2',
              rows: [[]]
            }
          }
        ]]
      };

      expect(service.containsModalInContainer(modalAtRootOfPage)).toBe(false);
    });

    it('should return true if a page contains a container containing a modal container', function () {
      var pageContainingContainerContainingModal = {
        type: 'page',
        name: 'page1',
        id: 'page-id-1',
        hasValidationError: false,
        rows: [[{
          $$id: 'pbContainer-1',
          $$parentContainerRow: {
            container: {
              type: 'page',
            }
          },
          type: 'container',
          id: 'pbContainer-1',
          container: {
            $$id: 'pbContainer-1',
            rows: [[{
              $$id: 'pbModalContainer-1',
              $$parentContainerRow: {
                container: {
                  type: 'container',
                }
              },
              type: 'modalContainer',
              id: 'pbModalContainer',
              container: {
                $$id: 'pbContainer-1',
                rows: [[]]
              }
            }]]
          }
        }]]
      };

      expect(service.containsModalInContainer(pageContainingContainerContainingModal)).toBe(true);
    });

    it('should return true if a tab contains modal container', function () {
      var pageContainingContainerContainingModal = {
        type: 'page',
        name: 'page1',
        id: 'page-id-1',
        hasValidationError: false,
        rows: [
          {
            $$id: 'tabsContainer',
            $$parentContainerRow: {
              container: {
                type: 'page',
              }
            },
            type: 'tabsContainer',
            tabList: [
              {
                type: 'tabContainer',
                id: 'pbTabContainer',
                container: {
                  rows: [[]]
                }
              },
              {
                $$id: 'pbContainer-1',
                $$parentContainerRow: {
                  container: {
                    type: 'container',
                  }
                },
                type: 'container',
                id: 'pbContainer-1',
                container: {
                  $$id: 'pbContainer-1',
                  rows: [[{
                    $$id: 'pbModalContainer-1',
                    $$parentContainerRow: {
                      container: {
                        type: 'container',
                      }
                    },
                    type: 'modalContainer',
                    id: 'pbModalContainer',
                    container: {
                      $$id: 'pbContainer-1',
                      rows: [[]]
                    }
                  }]]
                }
              }
            ]
          }]
      };

      expect(service.containsModalInContainer(pageContainingContainerContainingModal)).toBe(true);
    });




    it('should return true if a container is containing a modal container in the same row with another widget', function () {
      var modalWithWigetAndModalInRow = {
        type: 'page',
        name: 'page1',
        id: 'page-id-1',
        hasValidationError: false,
        $$parentContainerRow: {
          container: {
            type: 'page',
          }
        },
        rows: [[
          {
            $$id: 'pbContainer-1',
            $$parentContainerRow: {
              container: {
                type: 'container',
              }
            },
            type: 'container',
            id: 'pbContainer-1',
            container: {
              $$id: 'pbContainer-1',
              rows: [
                [
                  {
                    $$id: 1337,
                    $$parentContainerRow: {
                      container: {
                        type: 'container',
                      }
                    },
                    id: 'pbTextarea',
                    type: 'component'
                  },
                  {
                    $$id: 'pbModalContainer-1',
                    $$parentContainerRow: {
                      container: {
                        type: 'container',
                      }
                    },
                    type: 'modalContainer',
                    id: 'pbModalContainer',
                    container: {
                      $$id: 'pbContainer-1',
                      rows: [[]]
                    }
                  }
                ]
              ]
            }
          }
        ]]
      };
      expect(service.containsModalInContainer(modalWithWigetAndModalInRow)).toBe(true);
    });
  });

  describe('test if a modal is direct child of a container', function () {
    let modal = {
      $$id: 'pbModalContainer-1',
      type: 'modalContainer',
      id: 'pbModalContainer',
      container: {
        $$id: 'pbContainer-1',
        rows: [[{}]]
      }
    };

    var container = {
      type: 'container',
      id: 'pbContainer',
      $$id: 'pbContainer-2',
      rows: [[{}]]
    };

    it('should return true if a modal is not at the root of the page', function () {
      modal.$$parentContainerRow = {
        container: {
          type: 'container'
        }
      };
      expect(service.isModalInContainer(modal)).toBe(true);
    });

    it('should return false if a modal is at the root of the artifact', function () {
      modal.$$parentContainerRow = {
        container: {
          type: 'page'
        }
      };
      expect(service.isModalInContainer(modal)).toBe(false);
      modal.$$parentContainerRow = {
        container: {
          type: 'form'
        }
      };
      expect(service.isModalInContainer(modal)).toBe(false);
      modal.$$parentContainerRow = {
        container: {
          type: 'layout'
        }
      };
      expect(service.isModalInContainer(modal)).toBe(false);
    });

    it('should return false if it is not a modal', function () {
      expect(service.isModalInContainer(container)).toBe(false);
    });

  });

  describe('test if a component can move', function() {
    var componentItem, componentItem2;

    beforeEach(function() {
      componentItem = { $$id: 1337, type: 'component' };
      componentItem2 = { $$id: 42, type: 'component' };
    });

    it('should call the isChildOf method', function() {
      spyOn(service, 'isChildOf');
      service.isMovable(componentItem2, componentItem);
      expect(service.isChildOf).toHaveBeenCalledWith(1337, componentItem2);
    });

    it('should return true if it is a child a component and not the current item', function() {
      spyOn(service, 'isChildOf').and.returnValue(true);
      expect(service.isMovable(componentItem, componentItem2)).toBe(true);
    });

    it('should return true if it is not a child a component and not the current item', function() {
      spyOn(service, 'isChildOf').and.returnValue(false);
      expect(service.isMovable(componentItem, componentItem2)).toBe(true);
    });

    it('should return false if it is not a child a component and the current item', function() {
      spyOn(service, 'isChildOf').and.returnValue(false);
      expect(service.isMovable(componentItem, componentItem)).toBe(false);
    });

    it('should return false if it is a child not a component and the current item', function() {
      spyOn(service, 'isChildOf').and.returnValue(true);
      expect(service.isMovable(componentItem2, componentItem2)).toBe(false);
    });

    it('should return false if it is a child not a component and the same id but not the current item', function() {
      componentItem.$$id = 42;
      spyOn(service, 'isChildOf').and.returnValue(true);
      expect(service.isMovable(componentItem2, componentItem)).toBe(false);
    });
  });

  describe('find the width of a component', function() {
    it('should get the width of a component for the current resolution', function() {
      spyOn(resolutions, 'selected').and.returnValue({ key: 'foo' });
      expect(service.width.get({ dimension: { foo: 6 } })).toBe(6);
    });

    it('should get the width 1 if no component is passed as an argument', function() {
      expect(service.width.get()).toBe(1);
    });

    it('should set the size', function() {
      var component = { dimension: { foo: 6 } };

      spyOn(resolutions, 'selected').and.returnValue({ key: 'foo' });
      expect(service.width.get(component)).toBe(6);
      service.width.set(component, 8);
      expect(service.width.get(component)).toBe(8);
    });
  });

  describe('expose an api for columns', function() {

    var component;

    beforeEach(function() {
      component = {
        dimension: {
          xs: 7,
          foo: 8
        }
      };
    });

    describe('Find the size of a component', function() {
      it('should call resolutions .all', function() {
        spyOn(resolutions, 'all').and.callThrough();
        service.column.width(component);
        expect(resolutions.all).toHaveBeenCalled();
      });

      it('should based the count on the current selected resolution', function() {
        spyOn(resolutions, 'selected').and.callThrough();
        service.column.width(component);
        expect(resolutions.selected).toHaveBeenCalled();
      });

      it('should return the component col size for the current resolution', function() {
        resolutions.select('xs');
        expect(service.column.width(component)).toBe(7);
        resolutions.select('foo');
        expect(service.column.width(component)).toBe(8);
      });

      it('should return 12 if no with is set', function() {
        component.dimension = {};
        expect(service.column.width(component)).toBe(12);
      });

      it('should return defaultResolution resolution if no resolution is set', function() {
        component.dimension = { foo: 9 };
        expect(service.column.width(component)).toBe(9);
      });

      it('should return defaultResolution if no resolution is set', function() {
        $stateParams.resolution = '';
        expect(service.column.width(component)).toBe(8);
      });
    });

    it('should get a className based on the number of columns', function() {
      resolutions.select('foo');
      expect(service.column.className(component)).toBe('col-xs-8');
    });

    describe('set the default size for a col in a row', function() {
      var rows = {}, component;

      beforeEach(function() {

        component = {
          dimension: { xs: 12, foo: 12 }
        };

        rows.cas1 = [
          {
            dimension: { xs: 12, foo: 6 }
          },
          {
            dimension: { xs: 12, foo: 6 }
          }
        ];

        rows.cas2 = [
          {
            dimension: { xs: 12, foo: 4 }
          },
          {
            dimension: { xs: 12, foo: 3 }
          },
          {
            dimension: { xs: 12, foo: 3 }
          },
          {
            dimension: { xs: 12, foo: 3 }
          }
        ];

        rows.cas3 = [
          {
            dimension: { xs: 12, foo: 8 }
          }
        ];

        rows.cas6 = [];
      });

      it('should distribute equaly the new colsize for each component for lg and md size', function() {
        var currentRow = rows.cas1;
        currentRow.push(component);
        service.column.computeSizeItemInRow(currentRow);

        rows.cas1.forEach(function(compo) {
          expect(compo.dimension.xs).toBe(12);
          expect(compo.dimension.foo).toBe(4);
        });
      });

      it('should increase the colsize for the last component if equal colsize is not possible ', function() {
        var currentRow = rows.cas2;
        currentRow.push(component);
        service.column.computeSizeItemInRow(currentRow);

        rows.cas2.slice(0, -2).forEach(function(compo) {
          expect(compo.dimension.xs).toBe(12);
          expect(compo.dimension.foo).toBe(2);
        });

        // 5cols: | 2 | 2 | 2 | 3 | 3 | = 12
        rows.cas2.slice(-2).forEach(function(compo) {
          expect(compo.dimension.xs).toBe(12);
          expect(compo.dimension.foo).toBe(3);
        });
      });

      it('should fill the remaining space if there is empty space in row ', function() {
        var currentRow = rows.cas3;
        currentRow.push(component);
        service.column.computeSizeItemInRow(currentRow);

        var lastItem = currentRow[currentRow.length - 1];
        var firstItem = currentRow[0];

        expect(firstItem.dimension.xs).toBe(12);
        expect(firstItem.dimension.foo).toBe(8);

        expect(lastItem.dimension.xs).toBe(12);
        expect(lastItem.dimension.foo).toBe(4);
      });
    });
  });

  describe('isContainer', function() {
    it('should return true if a component is a container', function() {
      var container = {
        $$id: 'tab-5',
        type: 'container',
        rows: [[],[]]
      };
      expect(service.isContainer(container)).toBe(true);

      var tabContainer = {
        $$id: 'tabsContainer',
        type: 'tabsContainer',
        tabs: [{
          container: {
            rows: [[]]
          }
        }]
      };
      expect(service.isContainer(tabContainer)).toBe(true);

      var widget = {
        $$widget: {
          name: 'label'
        }
      };
      expect(service.isContainer(widget)).toBe(false);
    });
  });

  describe('isEmpty', function() {
    it('should return true if a container is empty', function() {
      var container = {
        rows: [[],[]]
      };
      expect(service.isEmpty(container)).toBe(true);
    });

    it('should return false if a container is not empty', function() {
      var container = {
        rows: [[],[{ 'name': 'titi' }]]
      };
      expect(service.isEmpty(container)).toBe(false);
    });
  });

  describe('getVisibleComponents', function() {
    it('should return a flat array of child components', function() {
      expect(service.getVisibleComponents(pageJson).length).toBe(8);
      var tabContainer = pageJson.rows[3][0];

      tabContainer.$$openedTab = tabContainer.tabList[1];
      expect(service.getVisibleComponents(pageJson).length).toBe(10);
    });
  });

  describe('isContainer', function() {
    it('should return false for fragment', function() {
      var fragment = {
        type: 'fragment',
        id: 'fragment-2',
        name: 'name',
        rows: []
      };
      expect(service.isContainer(fragment)).toBe(false);
    });
  });

  describe('modal containers test', function() {
    it ('should return true if a modal is inside a fragment', function() {
      var modalInFragment = {
        $$id: 'pbModalContainer-1',
        type: 'modalContainer',
        id: 'pbModalContainer',
        $$parentContainerRow: {
          container: {
            type: 'fragment'
          }
        },
        container: {
          $$id: 'pbContainer-1',
          rows: [[{}]]
        }
      };
      expect(service.isModalInContainer(modalInFragment)).toBe(true);
    });
  });
});
