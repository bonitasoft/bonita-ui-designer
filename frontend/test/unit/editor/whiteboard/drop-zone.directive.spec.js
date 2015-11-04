describe('dropZone directive', function() {

  'use strict';

  var $compile, element, container, row, template, scope, directiveScope, componentUtils;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function($injector) {

    var $rootScope = $injector.get('$rootScope');
    $compile = $injector.get('$compile');
    componentUtils = $injector.get('componentUtils');

    scope = $rootScope.$new();
    scope.editor = jasmine.createSpyObj('editor', [
      'selectRow',
      'addComponentToRow',
      'removeCurrentComponent',
      'dropElement',
      'moveAtPosition'
    ]);

    row = [];
    container = {
      rows: [row]
    };
    scope.container = container;
    scope.row = row;

    scope.editor.isContainerChildOfContainer = angular.noop;

    template = '<drop-zone editor="editor" componentIndex="componentIndex" row="row"></drop-zone>';
    element = $compile(template)(scope);
    scope.$apply();

    directiveScope = element.scope();
  }));

  it('should contains 2 drop zones', function() {
    expect(element.find('.dropZone').length).toBe(2);
  });

  it('should put right modifier on the second drop zone', function() {
    expect(element.find('.dropZone').last().hasClass('dropZone--right')).toBeTruthy();
  });

  it('should add a widget when the element is dropped before', function() {
    var data = { type: 'widget', widget: 'widget' };
    directiveScope.componentIndex = 1;
    directiveScope.dropBefore(data);

    expect(scope.editor.addComponentToRow).toHaveBeenCalledWith(data, container, row, 1);
  });

  it('should add a widget when the element is dropped after', function() {
    var data = { type: 'widget', widget: 'widget' };
    directiveScope.componentIndex = 2;
    directiveScope.dropAfter(data);
    expect(scope.editor.addComponentToRow).toHaveBeenCalledWith(data, container, row, 3);
  });

  it('should add a container when the element is dropped before', function() {
    var data = { type: 'container' };
    directiveScope.componentIndex = 1;
    directiveScope.dropBefore(data);

    expect(scope.editor.addComponentToRow).toHaveBeenCalledWith(data, container, row, 1);
  });

  it('should add a container when the element is dropped after', function() {
    var data = { type: 'container' };
    directiveScope.componentIndex = 4;
    directiveScope.dropAfter(data);
    expect(scope.editor.addComponentToRow).toHaveBeenCalledWith(data, container, row, 5);
  });

  it('should add a tabs container when the element is dropped before', function() {
    var data = { type: 'tabsContainer' };
    directiveScope.componentIndex = 2;
    directiveScope.dropBefore(data);

    expect(scope.editor.addComponentToRow).toHaveBeenCalledWith(data, container, row, 2);
  });

  it('should add a tabs container when the element is dropped after', function() {
    var data = { type: 'tabsContainer' };
    directiveScope.componentIndex = 2;
    directiveScope.dropAfter(data);

    expect(scope.editor.addComponentToRow).toHaveBeenCalledWith(data, container, row, 3);
  });

  describe('we drop a widget with a configuration', function() {

    var widgetConfig = {
      $$id: Date.now(),
      $$widget: { name: 'widget' },
      $$parentContainerRow: {
        row: []
      }
    }, isMovableBoolean = true;

    beforeEach(function() {
      spyOn(componentUtils,'isMovable').and.returnValue(isMovableBoolean);
    });

    it('should select the current row', function() {
      directiveScope.dropBefore(widgetConfig);
      expect(scope.editor.selectRow).toHaveBeenCalledWith(scope.container,[]);
    });

    it('should remove the current item from the row', function() {
      directiveScope.dropBefore(widgetConfig);
      expect(scope.editor.removeCurrentComponent).toHaveBeenCalledWith(widgetConfig);
    });

    it('should drop the current item in the row', function() {
      directiveScope.dropBefore(widgetConfig);
      expect(scope.editor.dropElement).toHaveBeenCalledWith(widgetConfig);
    });

    it('should drop the current item in the row', function() {
      directiveScope.dropBefore(widgetConfig);
      expect(scope.editor.moveAtPosition).toHaveBeenCalledWith(void 0,widgetConfig);
    });

    it('should drop the current item in the row if componentIndex is defined', function() {
      directiveScope.componentIndex = 1;
      directiveScope.dropAfter(widgetConfig);
      expect(scope.editor.moveAtPosition).toHaveBeenCalledWith(2,widgetConfig);
    });

    describe('change the position inside a row', function() {

      var widgetConfig2 = angular.copy(widgetConfig);
      var widgetConfig3 = angular.copy(widgetConfig);

      widgetConfig.$$widget.name = 'test-1';
      widgetConfig2.$$widget.name = 'test-2';
      widgetConfig3.$$widget.name = 'test-3';

      beforeEach(function() {
        directiveScope.row = [widgetConfig,widgetConfig2,widgetConfig3];
      });

      it('should not call the editor move position if the item cannot move', function() {
        isMovableBoolean = false;
        directiveScope.dropBefore(widgetConfig);
        expect(scope.editor.moveAtPosition).not.toHaveBeenCalled();
        isMovableBoolean = true;
      });

      describe('Item will not move', function() {

        describe('First item in the row', function() {

          it('should not move on before sur the second item', function() {
            directiveScope.componentIndex = 1;
            directiveScope.dropBefore(widgetConfig);
            expect(scope.editor.moveAtPosition).not.toHaveBeenCalled();
          });

          it('should not move on before first item', function() {
            directiveScope.componentIndex = 0;
            directiveScope.dropBefore(widgetConfig);
            expect(scope.editor.moveAtPosition).not.toHaveBeenCalled();
          });

          it('should not move on after first item', function() {
            directiveScope.componentIndex = 0;
            directiveScope.dropAfter(widgetConfig);
            expect(scope.editor.moveAtPosition).not.toHaveBeenCalled();
          });

        });

        describe('Second item in the row',function() {

          it('should not move on before last item', function() {
            directiveScope.componentIndex = 1;
            directiveScope.dropBefore(widgetConfig2);
            expect(scope.editor.moveAtPosition).not.toHaveBeenCalled();
          });

          it('should not move on after last item', function() {
            directiveScope.componentIndex = 1;
            directiveScope.dropAfter(widgetConfig2);
            expect(scope.editor.moveAtPosition).not.toHaveBeenCalled();
          });

          it('should not move on after sur the first item', function() {
            directiveScope.componentIndex = 0;
            directiveScope.dropAfter(widgetConfig2);
            expect(scope.editor.moveAtPosition).not.toHaveBeenCalled();
          });

          it('should not move on before sur the last item', function() {
            directiveScope.componentIndex = 2;
            directiveScope.dropBefore(widgetConfig2);
            expect(scope.editor.moveAtPosition).not.toHaveBeenCalled();
          });

        });

        describe('Last item in the row', function() {

          it('should not move on after sur the second item', function() {
            directiveScope.componentIndex = 1;
            directiveScope.dropAfter(widgetConfig3);
            expect(scope.editor.moveAtPosition).not.toHaveBeenCalled();
          });

          it('should not move on before last item', function() {
            directiveScope.componentIndex = 2;
            directiveScope.dropBefore(widgetConfig3);
            expect(scope.editor.moveAtPosition).not.toHaveBeenCalled();
          });

          it('should not move on after last item', function() {
            directiveScope.componentIndex = 2;
            directiveScope.dropAfter(widgetConfig3);
            expect(scope.editor.moveAtPosition).not.toHaveBeenCalled();
          });

        });
      });

      describe('Move an item in the row', function() {

        describe('First item', function() {

          it('should move at 1 if we are on the last item before', function() {
            directiveScope.componentIndex = 2;
            directiveScope.dropBefore(widgetConfig);
            expect(scope.editor.moveAtPosition).toHaveBeenCalledWith(1,widgetConfig);
          });

          it('should move at 1 if we are on the second item after', function() {
            directiveScope.componentIndex = 1;
            directiveScope.dropAfter(widgetConfig);
            expect(scope.editor.moveAtPosition).toHaveBeenCalledWith(1,widgetConfig);
          });

          it('should move at 2 if we are on the last item after', function() {
            directiveScope.componentIndex = 2;
            directiveScope.dropAfter(widgetConfig);
            expect(scope.editor.moveAtPosition).toHaveBeenCalledWith(2,widgetConfig);
          });

        });

        describe('Second item', function() {

          it('should move at 0 if we are on the first item before', function() {
            directiveScope.componentIndex = 0;
            directiveScope.dropBefore(widgetConfig2);
            expect(scope.editor.moveAtPosition).toHaveBeenCalledWith(0,widgetConfig2);
          });

          it('should move at 0 if we are on the first item before', function() {
            directiveScope.componentIndex = 2;
            directiveScope.dropAfter(widgetConfig2);
            expect(scope.editor.moveAtPosition).toHaveBeenCalledWith(2,widgetConfig2);
          });

        });

        describe('Last item', function() {

          it('should move at 0 if we are on the first item before', function() {
            directiveScope.componentIndex = 0;
            directiveScope.dropBefore(widgetConfig3);
            expect(scope.editor.moveAtPosition).toHaveBeenCalledWith(0,widgetConfig3);
          });

          it('should move at 0 if we are on the first item after', function() {
            directiveScope.componentIndex = 0;
            directiveScope.dropAfter(widgetConfig3);
            expect(scope.editor.moveAtPosition).toHaveBeenCalledWith(1,widgetConfig3);
          });

          it('should move at 1 if we are on the second item after', function() {
            directiveScope.componentIndex = 1;
            directiveScope.dropBefore(widgetConfig3);
            expect(scope.editor.moveAtPosition).toHaveBeenCalledWith(1,widgetConfig3);
          });

        });

      });

    });

  });

  describe('Can we drag and drop this item here ?', function() {

    var dataWidget = {
      $$id: 1337,
      name: 'toto',
      $$widget: {},
      $$parentContainerRow: {
        container: {},
        row: []
      }
    };

    it('should try to move an item if we drop a widget with a config', function() {

      var item = angular.copy(dataWidget);
      spyOn(componentUtils,'isMovable');
      directiveScope.dropBefore(item);
      expect(componentUtils.isMovable).toHaveBeenCalled();
    });
  });

});
