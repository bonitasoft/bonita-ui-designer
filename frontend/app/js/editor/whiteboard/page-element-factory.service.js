(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.editor.whiteboard')
    .service('pageElementFactory', pageElementFactory);

  function pageElementFactory(resolutions, properties) {

    return {
      createWidgetElement: createWidgetElement,
      createContainerElement: createContainerElement,
      createTabsContainerElement: createTabsContainerElement,
      createModalContainerElement: createModalContainerElement,
      createFormContainerElement: createFormContainerElement,
      createTabElement: createTabElement
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
      var element = createElement('tabsContainer', tabsContainer);
      return angular.extend(element, {
        tabs: ['Tab 1', 'Tab 2'].map(createTabElement)
      });
    }

    /**
     * Creates a new tab for the given tabs container, with the given title
     */
    function createTabElement(title) {
      return {
        title: title,
        tabId: title.replace(/\s/g,'').toLowerCase(),
        container: {
          id: 'pbContainer',
          type: 'container',
          rows: [
            []
          ]
        }
      };
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
