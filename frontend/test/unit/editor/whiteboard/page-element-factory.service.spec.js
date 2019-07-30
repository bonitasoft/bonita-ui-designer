(function () {

  'use strict';

  describe('page element factory', function () {

    var service;

    beforeEach(angular.mock.module('bonitasoft.designer.editor'));

    beforeEach(inject(function (pageElementFactory, components) {
      service = pageElementFactory;
      components = components;
      spyOn(components, 'getById').and.returnValue({
        component: {
          id: 'pbTabContainer',
          properties: [
            {
              name: 'title',
              bond: 'interpolation',
              defaultValue: 'Tab X'
            },
            {
              name: 'disabled',
              bond: 'constant',
              defaultValue: false
            }
          ]
        }
      });
    }));

    it('should create a page element for a widget', function () {
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
        dimension: {xs: 12},
        propertyValues: {
          robert: {type: 'constant', value: 'manger'}
        }
      });
    });

    it('should create a page element with component type for a widget', function () {
      var widget = {
        id: 'aWidget',
        type: 'widget'
      };

      var element = service.createWidgetElement(widget);

      expect(element).toEqual({
        id: 'aWidget',
        type: 'component',
        dimension: {xs: 12},
        propertyValues: {}
      });
    });

    it('should create a page element for a container', function () {
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
        dimension: {xs: 12},
        propertyValues: {
          robert: {type: 'constant', value: 'manger'}
        },
        rows: [[]]
      });
    });

    it('should create a page element for a tabsContainer', function () {
      let tabsContainer = {
        id: 'pbTabsContainer',
        properties: [
          {
            name: 'vertical',
            bond: 'constant',
            defaultValue: false
          },
          {
            name: 'type',
            bond: 'constant',
            defaultValue: 'tabs'
          }
        ]
      };

      let element = service.createTabsContainerElement(tabsContainer);
      console.log(element);
      expect(element).toEqual({
        id: 'pbTabsContainer',
        type: 'tabsContainer',
        dimension: Object({xs: 12}),
        propertyValues: Object({
          vertical: Object({type: 'constant', value: false}),
          type: Object({type: 'constant', value: 'tabs'})
        }),
        tabList: [Object({
          id: 'pbTabContainer',
          type: 'tabContainer',
          dimension: Object({xs: 12}),
          propertyValues: Object({
            title: Object({type: 'interpolation', value: 'Tab 1'}),
            disabled: Object({type: 'constant', value: false})
          }),
          container: Object({id: 'pbContainer', type: 'container', rows: [[]]})
        }), Object({
          id: 'pbTabContainer',
          type: 'tabContainer',
          dimension: Object({xs: 12}),
          propertyValues: Object({title: Object({type: 'interpolation', value: 'Tab 2'}),
            disabled: Object({type: 'constant', value: false})
          }),
          container: Object({id: 'pbContainer', type: 'container', rows: [[]]})
        })]
      });
    });

    it('should create a page element for a tabContainer', function () {
      let tabContainer = {
        id: 'pbTabContainer',
        properties: [
          {
            name: 'title',
            bond: 'interpolation',
            defaultValue: 'Tab 1'
          },
          {
            name: 'disabled',
            bond: 'constant',
            defaultValue: false
          }

        ]
      };

      let element = service.createTabContainerElement(tabContainer, 'MyTitle');

      expect(element).toEqual({
        id: 'pbTabContainer',
        type: 'tabContainer',
        dimension: Object({xs: 12}),
        propertyValues: Object({title: Object({type: 'interpolation', value: 'MyTitle'}), disabled: Object({type: 'constant', value: false})}),
        container: Object({id: 'pbContainer', type: 'container', rows: [[]]})
      });
    });

    it('should create a page element for a form container', function () {
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
        dimension: {xs: 12},
        propertyValues: {
          robert: {type: 'constant', value: 'manger'}
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
