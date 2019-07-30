(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.editor.whiteboard')
    .service('pageElementFactory', pageElementFactory);

  function pageElementFactory(resolutions, properties, components) {

    return {
      createWidgetElement: createWidgetElement,
      createContainerElement: createContainerElement,
      createTabsContainerElement: createTabsContainerElement,
      createModalContainerElement: createModalContainerElement,
      createFormContainerElement: createFormContainerElement,
      createTabContainerElement: createTabContainerElement
    };

    function createElement(type, definition) {
      return {
        id: definition.id,
        type: type,
        dimension: resolutions.getDefaultDimension(),
        propertyValues: properties.computeValues(definition.properties)
      };
    }

    /**
     * Create a new page element from a widget definition
     * @param  {Object} widget    Widget configuration
     * @param  {Array} parentRow  parent row container
     * @return {Object}           New component to add to the whiteboard
     */
    function createWidgetElement(widget) {
      return createElement(getType(widget), widget);
    }

    function getType(component) {
      // for now, page widget elements have type 'component'
      return !component.type || component.type === 'widget' ? 'component' : component.type;
    }

    function createContainerElement(container) {
      var element = createElement('container', container);
      return angular.extend(element, {
        rows: [
          []
        ]
      });
    }

    function createTabsContainerElement(tabsContainer) {
      let tabsContainerElement = createElement('tabsContainer', tabsContainer);

      let tabContainer = components.getById('pbTabContainer').component;
      return angular.extend(tabsContainerElement, {
        tabList: ['Tab 1', 'Tab 2'].map(title => createTabContainerElement(tabContainer, title))
      });
    }

    function createTabContainerElement(tabContainer, title) {
      let element = createElement('tabContainer', tabContainer);
      if (title) {
        element.propertyValues.title.value = title;
      }

      return angular.extend(element, {
        container: {
          id: 'pbContainer',
          type: 'container',
          rows: [
            []
          ]
        }
      });
    }

    function createFormContainerElement(formContainer) {
      var element = createElement('formContainer', formContainer);
      return angular.extend(element, {
        container: {
          id: 'pbContainer',
          type: 'container',
          rows: [
            []
          ]
        }
      });
    }

    function createModalContainerElement(modalContainer) {
      var element = createElement('modalContainer', modalContainer);
      return angular.extend(element, {
        container: {
          id: 'pbContainer',
          type: 'container',
          rows: [
            []
          ]
        }
      });
    }

  }
})();
