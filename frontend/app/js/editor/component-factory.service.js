/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
(function () {

  'use strict';

  angular
    .module('bonitasoft.designer.editor')
    .service('componentFactory', componentFactory);

  function componentFactory(paletteService, widgetFactory, properties, resolutions, gettextCatalog, gettext) {

    var counters = {};
    var service = {
      getNextId: getNextId,
      initializePage: initializePage,

      createWidget: createWidget,
      initializeWidget: initializeWidget,

      createContainer: createContainer,
      initializeContainer: initializeContainer,

      createTabsContainer: createTabsContainer,
      initializeTabsContainer: initializeTabsContainer,
      createNewTab: createNewTab,
      initializeTab: initializeTab,

      createFormContainer: createFormContainer,
      initializeFormContainer: initializeFormContainer,

      getPaletteContainers: getPaletteContainers,
      paletteWrapper: paletteWrapper
    };
    return service;

    /**
     * [getNextId description]
     * @param  {[type]} type [description]
     * @return {[type]}      [description]
     */
    function getNextId(type) {
      if (counters.hasOwnProperty(type)) {
        counters[type] += 1;
      } else {
        counters[type] = 0;
      }
      return type + '-' + counters[type];
    }

    /**
     * @internal
     * Initializes a row coming from the server.
     */
    function initializeRow(container, row) {
      var parentContainerRow = {
        container: container,
        row: row
      };
      angular.forEach(row, function (component) {
        paletteService.init(component, parentContainerRow);
      });
    }


    function initializePage(page) {
      page.rows.forEach(initializeRow.bind(null, page));
    }


    /**
     * Create a new component from a widget definition
     * @param  {Object} widget    Widget configuration
     * @param  {Array} parentRow  parent row container
     * @return {Object}           New component to add to the whiteboard
     */
    function createWidget(widget, parentRow) {
      var item = {
        id: widget.id,
        type: 'component',
        dimension: resolutions.getDefaultDimension(),
        propertyValues: properties.computeValues(widget.properties)
      };
      service.initializeWidget(widget, item, parentRow);
      return item;
    }

    /**
     * Initialize (mutate) a component to be used in whiteboard
     * component can come from a page definition or from createWidget
     * @param  {Object} widget    Widget configuration
     * @param  {Object} item      Widget instance
     * @param  {Object} parentRow parent row container
     */
    function initializeWidget(widget, item, parentRow) {
      // The $$ prefix makes sure the attribute is not serialized to JSON
      // $$id is only used by e2e tests
      // $$widget is a direct reference to the widget identified by widgetId. Only widgetId needs to be serialized
      // $$templateUrl is  used in container.html to display the component
      // $$parentContainerRow is a backward reference to the containing container and row, which is only useful in the
      // editor, but must not and can not be serialized (cyclic reference)
      angular.extend(item, {
        $$id: getNextId('component'),
        $$widget: angular.copy(widget), // make sure to render all properties every time we select a component
        $$templateUrl: 'js/editor/workspace/component-template.html',
        $$propertiesTemplateUrl: 'js/editor/properties-panel/component-properties-template.html',
        $$parentContainerRow: parentRow
      });
    }


    function createContainer(container, parentRow) {
      var item = {
        type: 'container',
        dimension: resolutions.getDefaultDimension(),
        propertyValues: properties.computeValues(container.properties),
        rows: [
          []
        ]
      };

      service.initializeContainer(item, parentRow);
      return item;
    }

    function initializeContainer(container, parentRow) {
      angular.extend(container, {
        $$id: getNextId('container'),
        $$widget: properties.addCommonPropertiesTo(widgetFactory.createContainerWidget()),
        $$templateUrl: 'js/editor/workspace/container-template.html',
        $$propertiesTemplateUrl: 'js/editor/properties-panel/container-properties-template.html',
        $$parentContainerRow: parentRow
      });

      container.rows.forEach(initializeRow.bind(null, container));
    }

    function createTabsContainer(tabsContainer, parentRow) {
      var container = {
        type: 'tabsContainer',
        dimension: resolutions.getDefaultDimension(),
        propertyValues: properties.computeValues(tabsContainer.properties)
      };
      container.tabs = ['Tab 1', 'Tab 2'].map(createNewTab);

      service.initializeTabsContainer(container, parentRow);
      return container;
    }

    function initializeTabsContainer(container, parentRow) {
      angular.extend(container, {
        $$id: getNextId('tabsContainer'),
        $$widget: properties.addCommonPropertiesTo(widgetFactory.createTabsContainerWidget()),
        $$templateUrl: 'js/editor/workspace/tabs-container-template.html',
        $$propertiesTemplateUrl: 'js/editor/properties-panel/component-properties-template.html',
        $$parentContainerRow: parentRow
      });

      container.tabs.forEach(function (tab) {
        service.initializeTab(tab, container);
        service.initializeContainer(tab.container);
      });
    }

    function initializeTab(tab, tabsContainer) {
      angular.extend(tab, {
          $$parentTabsContainer: tabsContainer,
          $$widget: {
            name: 'Tab'
          },
          $$propertiesTemplateUrl: 'js/editor/properties-panel/tab-properties-template.html'
        }
      );
    }

    /**
     * Creates a new tab for the given tabs container, with the given title
     */
    function createNewTab(title) {
      return {
        title: title,
        container: {
          type: 'container',
          rows: [
            []
          ]
        }
      };
    }

    function createFormContainer(formContainer, parentRow) {
      var container = {
        type: 'formContainer',
        dimension: resolutions.getDefaultDimension(),
        propertyValues: properties.computeValues(formContainer.properties),
        container: {
          type: 'container',
          rows: [
            []
          ]
        }
      };

      service.initializeFormContainer(container, parentRow);
      return container;
    }


    function initializeFormContainer(formContainer, parentRow) {
      angular.extend(formContainer, {
        $$id: getNextId('formContainer'),
        $$widget: properties.addCommonPropertiesTo(widgetFactory.createFormContainerWidget()),
        $$templateUrl: 'js/editor/workspace/form-container-template.html',
        $$propertiesTemplateUrl: 'js/editor/properties-panel/component-properties-template.html',
        $$parentContainerRow: parentRow
      });

      service.initializeContainer(formContainer.container);
    }


    function getPaletteContainers() {
      var container = properties.addCommonPropertiesTo(widgetFactory.createContainerWidget());
      var tabsContainer = properties.addCommonPropertiesTo(widgetFactory.createTabsContainerWidget());
      var formContainer = properties.addCommonPropertiesTo(widgetFactory.createFormContainerWidget());
      return [
        {
          sectionName: gettext('widgets'),
          sectionOrder: 1,
          component: container,
          init: initializeContainer,
          create: createContainer.bind(null, container)
        }, {
          sectionName: gettext('widgets'),
          sectionOrder: 1,
          component: tabsContainer,
          init: initializeTabsContainer,
          create: createTabsContainer.bind(null, tabsContainer)
        }, {
          sectionName: gettext('widgets'),
          sectionOrder: 1,
          component: formContainer,
          init: initializeFormContainer,
          create: createFormContainer.bind(null, formContainer)
        }
      ];
    }

    function paletteWrapper(name, order, component) {
      var extended = properties.addCommonPropertiesTo(component);
      return {
        component: extended,
        sectionName: name,
        sectionOrder: order,
        init: initializeWidget.bind(null, extended),
        create: createWidget.bind(null, extended)
      };
    }

  }
})
();
