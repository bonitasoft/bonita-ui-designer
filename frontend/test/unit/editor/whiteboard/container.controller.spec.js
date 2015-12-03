describe('ContainerDirectiveCtrl', function() {
  'use strict';

  var $scope, componentUtils,  $element, rootScope, init;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function($rootScope, $controller, $injector) {

    componentUtils = $injector.get('componentUtils');

    rootScope = $rootScope;
    $element = {
      attr: angular.noop,
      removeClass: angular.noop,
      addClass: angular.noop
    };

    $scope = $rootScope.$new();
    init = function() {
      return $controller('ContainerDirectiveCtrl', {
        $scope: $scope,
        $rootScope: rootScope,
        $element: $element
      });
    };
  }));

  var mockEditor = {};

  beforeEach(function() {

    mockEditor = jasmine.createSpyObj('mockEditor', [
      'addComponentToRow',
      'removeCurrentComponent',
      'selectRow',
      'removeCurrentRow',
      'dropElement'
    ]);

    mockEditor.isCurrentRow = function() {
      return true;
    };
    mockEditor.isCurrentComponent = function() {
      return true;
    };

    $scope.editor = mockEditor;
    $scope.container = {};
  });

  it('should verify that a container is empty', function() {
    init();
    var container = {
      rows: [[]]
    };

    expect($scope.isEmpty(container)).toBeTruthy();
  });

  it('should verify that a container is not empty', function() {
    init();
    var container = {
      rows: [[{ name: 'test' }]]
    };

    $scope.isEmpty(container);

    expect($scope.isEmpty(container)).toBeFalsy();
  });

  it('should verify that a container is repeated when property value repeatedCollection has a value', function() {
    init();
    var container = {
      propertyValues: {
        repeatedCollection: {
          value: 'aValue'
        }
      }
    };

    expect($scope.isRepeated(container)).toBeTruthy();
  });

  it('should verify that a container is not repeated otherwise', function() {
    init();
    var container = {
      propertyValues: {
        repeatedCollection: {
          value: ''
        }
      }
    };

    expect($scope.isRepeated(container)).toBeFalsy();
    expect($scope.isRepeated({})).toBeFalsy();
  });

  describe('We can move rows', function() {

    beforeEach(function() {
      init();
    });

    it('should move row up', function() {
      var row1 = [], row2 = [],row3 = [];

      $scope.container = {
        rows: [row1, row2, row3]
      };

      expect($scope.moveRowUpVisible(row1)).toBeFalsy();
      expect($scope.moveRowUpVisible(row2)).toBeTruthy();
      expect($scope.moveRowUpVisible(row3)).toBeTruthy();

      var event = {
        stopPropagation: function() {
        }
      };

      $scope.moveRowUp(row2, event);

      expect($scope.container.rows[0]).toBe(row2);
      expect($scope.container.rows[1]).toBe(row1);
    });

    it('should move row down', function() {
      var row1 = [], row2 = [], row3 = [];

      $scope.container = {
        rows: [row1, row2, row3]
      };

      expect($scope.moveRowDownVisible(row1)).toBeTruthy();
      expect($scope.moveRowDownVisible(row2)).toBeTruthy();
      expect($scope.moveRowDownVisible(row3)).toBeFalsy();

      var event = {
        stopPropagation: function() {
        }
      };
      $scope.moveRowDown(row2, event);

      expect($scope.container.rows[1]).toBe(row3);
      expect($scope.container.rows[2]).toBe(row2);
    });

  });

  describe('We can edit rows', function() {
    beforeEach(function() {
      init();
    });
    it('should select the currrent row when we remove a row', function() {
      var row = [{ name: 'test' }];
      $scope.removeRow(row);
      expect(mockEditor.selectRow).toHaveBeenCalledWith({},row);
    });

    it('should select remove a row', function() {
      var row = [{ name: 'test' }];
      $scope.removeRow(row);
      expect(mockEditor.removeCurrentRow).toHaveBeenCalledWith();
    });

  });

  describe('Drag and drop of items', function() {
    beforeEach(function() {
      init();
    });

    describe('Drop an item', function() {

      var dataWidget = {
        $$id: Date.now(),
        name: 'toto',
        $$widget: {}
      };

      it('should not do anything if the current item is not movable', function() {
        spyOn(componentUtils, 'isMovable').and.returnValue(false);
        $scope.dropItem(dataWidget, []);
        expect(mockEditor.selectRow).not.toHaveBeenCalled();
      });

      it('should select the current row on drop', function() {
        spyOn(componentUtils, 'isMovable').and.returnValue(true);
        $scope.dropItem(dataWidget, []);
        expect(mockEditor.selectRow).toHaveBeenCalledWith({},[]);
      });

      it('should remove the current item on drop', function() {
        spyOn(componentUtils, 'isMovable').and.returnValue(true);
        $scope.dropItem(dataWidget, []);
        expect(mockEditor.removeCurrentComponent).toHaveBeenCalledWith(dataWidget, []);
      });

    });

    describe('Drop at the end of a row', function() {

      var mockItemDragged = {
        widget: {},
        type: 'widget'
      };

      it('should select the current row if the item has a configuration and is movable', function() {
        var data = {
          $$id: Date.now(),
          $$widget: {}
        };
        spyOn(componentUtils, 'isMovable').and.returnValue(true);
        $scope.dropAtEndOfTheRow(data,{},[]);
        expect(mockEditor.selectRow).toHaveBeenCalledWith({},[]);
        expect(mockEditor.dropElement).toHaveBeenCalledWith(data);
      });

      it('should do nothing if the item has a config and not movable', function() {
        var data = {
          $$id: Date.now(),
          $$widget: {}
        };
        spyOn(componentUtils, 'isMovable').and.returnValue(false);
        $scope.dropAtEndOfTheRow(data,{},[]);
        expect(mockEditor.selectRow).not.toHaveBeenCalled();
        expect(mockEditor.dropElement).not.toHaveBeenCalled();
        expect(mockEditor.addComponentToRow).not.toHaveBeenCalled();
      });

      describe('The item has no configuration', function() {

        it('should select the current row', function() {
          $scope.dropAtEndOfTheRow(mockItemDragged,{},[]);
          expect(mockEditor.addComponentToRow).toHaveBeenCalledWith(mockItemDragged,{},[]);
        });

        it('should add a widget if the type is widget', function() {
          $scope.dropAtEndOfTheRow(mockItemDragged,{},[]);
          expect(mockEditor.addComponentToRow).toHaveBeenCalledWith(mockItemDragged,{},[]);
        });

        it('should not drop it if the container is not movable', function() {

          var item = {
            $$widget: {}
          };
          $scope.container = item;
          item.type = 'container';

          $scope.dropAtEndOfTheRow(item, []);
          expect(mockEditor.selectRow).not.toHaveBeenCalled();
        });
      });

      describe('We can drop before or after a position', function() {

        beforeEach(function() {
          $scope.dropAtEndOfTheRow = angular.noop;
          spyOn($scope,'dropAtEndOfTheRow');
        });

        it('should not create a new row before if we push a non-movable container', function() {
          var rows = ['name','test','toto'];
          spyOn(componentUtils, 'isMovable').and.returnValue(false);
          $scope.dropBeforeRow({},{},1,rows);
          expect(rows.length).toBe(3);
          expect(Array.isArray(rows[1])).toBe(false);
        });

        it('should not create a new row after if we push a non-movable container', function() {
          var rows = ['name','test','toto'];
          spyOn(componentUtils, 'isMovable').and.returnValue(false);
          $scope.dropBeforeRow({},{},1,rows);
          expect(rows.length).toBe(3);
          expect(Array.isArray(rows[1])).toBe(false);
        });

        it('should push the content of a rows at a position if we drag before it', function() {

          var rows = ['name','test','toto'];
          spyOn(componentUtils, 'isMovable').and.returnValue(false);
          $scope.dropAfterRow({},{},1,rows);
          expect(rows.length).toBe(3);
          expect(Array.isArray(rows[2])).toBe(false);
        });

        it('should trigger dropAtEndOfTheRow if we drag before it', function() {

          var rows = ['name','test','toto'];
          spyOn(componentUtils, 'isMovable').and.returnValue(true);
          $scope.dropBeforeRow({},{},1,rows);
          expect($scope.dropAtEndOfTheRow).toHaveBeenCalledWith({},{},rows[1]);
        });

        it('should push the content of a rows after a position if we drag before it', function() {

          var rows = ['name','test','toto'];
          spyOn(componentUtils, 'isMovable').and.returnValue(true);
          $scope.dropAfterRow({},{},1,rows);
          expect(rows.length).toBe(4);
          expect(Array.isArray(rows[2])).toBe(true);
        });

        it('should trigger dropAtEndOfTheRow if we drag before it', function() {

          var rows = ['name','test','toto'];
          spyOn(componentUtils, 'isMovable').and.returnValue(true);
          $scope.dropAfterRow({},{},1,rows);
          expect($scope.dropAtEndOfTheRow).toHaveBeenCalledWith({},{},rows[2]);
        });

        it('should update the array if the currentComponent has an ID. - after:component', function() {

          $scope.component = { id: 'test' };
          var rows = ['name','test','toto'];
          spyOn(componentUtils, 'isMovable').and.returnValue(true);
          $scope.dropAfterRow({},{},1,rows);
          expect(rows.length).toBe(4);
        });

        it('should update the array if the currentComponent has an ID. - before:component', function() {

          $scope.component = { id: 'test' };
          var rows = ['name','test','toto'];
          spyOn(componentUtils, 'isMovable').and.returnValue(true);
          $scope.dropBeforeRow({},{},1,rows);
          expect(rows.length).toBe(4);
        });

        it('should update the array if the currentComponent has an ID. - after:container', function() {

          $scope.container = { id: 'test' };
          var rows = ['name','test','toto'];
          spyOn(componentUtils, 'isMovable').and.returnValue(true);
          $scope.dropAfterRow({},{},1,rows);
          expect(rows.length).toBe(4);
        });

        it('should update the array if the currentComponent has an ID. - before:container', function() {

          $scope.container = { id: 'test' };
          var rows = ['name','test','toto'];
          spyOn(componentUtils, 'isMovable').and.returnValue(true);
          $scope.dropBeforeRow({},{},1,rows);
          expect(rows.length).toBe(4);
        });

      });

    });

  });

});
