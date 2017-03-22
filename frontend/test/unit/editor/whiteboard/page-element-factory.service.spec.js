(function() {

  'use strict';

  describe('page element factory', function() {

    var service;

    beforeEach(angular.mock.module('bonitasoft.designer.editor'));

    beforeEach(inject(function(pageElementFactory) {
      service = pageElementFactory;
    }));

    it('should create a page element for a widget', function() {
      var widget = {
        id: 'aWidget',
        properties: [
          {
            name: 'robert',
            bond: 'constant',
            defaultValue: 'manger'
          }
        ]
      };

      var element = service.createWidgetElement(widget);

      expect(element).toEqual({
        id: 'aWidget',
        type: 'component',
        dimension: { xs: 12 },
        propertyValues: {
          robert: { type: 'constant', value: 'manger' }
        }
      });
    });

    it('should create a page element with component type for a widget', function() {
      var widget = {
        id: 'aWidget',
        type: 'widget'
      };

      var element = service.createWidgetElement(widget);

      expect(element).toEqual({
        id: 'aWidget',
        type: 'component',
        dimension: { xs: 12 },
        propertyValues: {}
      });
    });

    it('should create a page element for a container', function() {
      var container = {
        id: 'pbContainer',
        properties: [
          {
            name: 'robert',
            bond: 'constant',
            defaultValue: 'manger'
          }
        ]
      };

      var element = service.createContainerElement(container);

      expect(element).toEqual({
        id: 'pbContainer',
        type: 'container',
        dimension: { xs: 12 },
        propertyValues: {
          robert: { type: 'constant', value: 'manger' }
        },
        rows: [[]]
      });
    });

    it('should create a page element for a tabsContainer', function() {
      var tabsContainer = {
        id: 'pbTabsContainer',
        properties: [
          {
            name: 'robert',
            bond: 'constant',
            defaultValue: 'manger'
          }
        ]
      };

      var element = service.createTabsContainerElement(tabsContainer);

      expect(element).toEqual({
        id: 'pbTabsContainer',
        type: 'tabsContainer',
        dimension: { xs: 12 },
        propertyValues: {
          robert: { type: 'constant', value: 'manger' }
        },
        tabs: [
          {
            title: 'Tab 1',
            container: {
              id: 'pbContainer',
              type: 'container',
              rows: [
                []
              ]
            }
          },
          {
            title: 'Tab 2',
            container: {
              id: 'pbContainer',
              type: 'container',
              rows: [
                []
              ]
            }
          }
        ]
      });
    });

    it('should create a page element for a tab', function() {
      var title = 'tab';

      var tab = service.createTabElement(title);

      expect(tab).toEqual({
        title: 'tab',
        container: {
          id: 'pbContainer',
          type: 'container',
          rows: [
            []
          ]
        }
      });
    });

    it('should create a page element for a form container', function() {
      var formContainer = {
        id: 'pbFormContainer',
        properties: [
          {
            name: 'robert',
            bond: 'constant',
            defaultValue: 'manger'
          }
        ]
      };

      var element = service.createFormContainerElement(formContainer);

      expect(element).toEqual({
        id: 'pbFormContainer',
        type: 'formContainer',
        dimension: { xs: 12 },
        propertyValues: {
          robert: { type: 'constant', value: 'manger' }
        },
        container: {
          id: 'pbContainer',
          type: 'container',
          rows: [
            []
          ]
        }
      });
    });
  });

})();
