(function() {
  'use strict';

  describe('editor-palette directive', function() {
    var $compile, $scope, element, controller, paletteService, $controller;
    var paletteData = [{
      name: 'section1',
      order: 1,
      widgets: [{
        name: 'widget1'
      }, {
        name: 'widget1'
      }, {
        name: 'widget1'
      }, {
        name: 'widget1'
      }, {
        name: 'widget1'
      }, {
        name: 'widget1'
      }, {
        name: 'widget1'
      }, {
        name: 'widget1'
      }, {
        name: 'widget1'
      }, {
        name: 'widget1'
      }, {
        name: 'widget1'
      }]
    }, {
      name: 'section2',
      order: 2,
      widgets: [{
        name: 'widget1'
      }, {
        name: 'widget1'
      }, {
        name: 'widget1'
      }]
    }];

    beforeEach(angular.mock.module('bonitasoft.designer.editor.palette'));
    beforeEach(inject(function($injector) {
      $compile = $injector.get('$compile');
      $controller = $injector.get('$controller');
      $scope = $injector.get('$rootScope').$new();
      paletteService = $injector.get('paletteService');

      $scope.resizePaletteHandler = function() {};
      spyOn($scope, 'resizePaletteHandler');

      spyOn(paletteService, 'getSections').and.returnValue(paletteData);

      var template = '<div editor-palette on-resize="resizePaletteHandler(isClosed, isNarrow)" ></div>';
      element = $compile(template)($scope);
      $scope.$digest();
      controller = element.controller('editorPalette');
    }));

    it('should display palette sections', function() {
      var sections = element.find('.Palette-section');
      var widgets = element.find('.PaletteItem');
      expect(sections.length).toBe(paletteData.length);
      expect(widgets.length).toBe(paletteData[0].widgets.length);
    });

    it('should select the first sections as default', function() {
      var sections = element.find('.Palette-section');
      expect(sections[0].getAttribute('class')).toMatch('Palette-section--active');
    });

    it('should change a sections', function() {
      var sections = element.find('.Palette-section');
      sections.eq(1).click();
      expect(sections[1].getAttribute('class')).toMatch('Palette-section--active');

      var widgets = element.find('.PaletteItem');
      expect(widgets.length).toBe(paletteData[1].widgets.length);

      expect($scope.resizePaletteHandler).toHaveBeenCalledWith(false, true);
    });

    it('should close a sections', function() {
      var sections = element.find('.Palette-section');
      sections.eq(0).click();
      expect(sections[0].getAttribute('class')).not.toMatch('Palette-section--active');

      var widgets = element.find('.Palette-widgets');
      expect(widgets.length).toBe(0);
      expect($scope.resizePaletteHandler).toHaveBeenCalledWith(true, false);
    });

    it('should display sections with less than 10 widgets as narrow', function() {
      element.find('.Palette-section').eq(1).click();
      var paletteWidgets = element.find('.Palette-widgets');
      expect(paletteWidgets[0].getAttribute('class')).toMatch('Palette-widgets--narrow');
    });

    describe('controller', function() {
      it('should return sections', function() {
        expect(controller.sections).toEqual(paletteData);
      });

      it('should expose currentSection', function() {
        expect(controller.currentSection).toBe(paletteData[0]);
      });

      it('should toggle a section', function() {
        controller.toggleSection(paletteData[1]);
        expect(controller.currentSection).toBe(paletteData[1]);
        expect($scope.resizePaletteHandler).toHaveBeenCalledWith(false, true);
      });

      it('should close a section if currentSection is open', function() {
        controller.toggleSection(paletteData[0]);
        expect(controller.currentSection).toBe(undefined);
        expect($scope.resizePaletteHandler).toHaveBeenCalledWith(true, false);
      });

      it('should return correct active section', function() {
        expect(controller.isActiveSection(paletteData[0])).toBe(true);
        expect(controller.isActiveSection(paletteData[1])).toBe(false);
      });

      it('should return correct active section', function() {
        expect(controller.isActiveSection(paletteData[0])).toBe(true);
        expect(controller.isActiveSection(paletteData[1])).toBe(false);
      });

      it('should return correct narrow value', function() {
        expect(controller.isNarrow()).toBe(false);
        controller.currentSection = paletteData[1];
        expect(controller.isNarrow()).toBe(true);
      });

      it('should return correct narrow value', function() {
        expect(controller.isClosed()).toBe(false);
        controller.currentSection = undefined;
        expect(controller.isClosed()).toBe(true);
      });

      it('should get section icon class name', function() {
        var className = controller.getIconClassName({
          name: 'custom widget'
        });

        expect(className).toBe('ui-customwidget');
      });
    });
  });
})();
