angular.module('pb.services')
  .service('componentFactory', function (paletteService, widgetFactory, commonParams, resolutions, gettextCatalog) {

    'use strict';

    var counters = {};
    var service = this;

    /**
     * [getNextId description]
     * @param  {[type]} type [description]
     * @return {[type]}      [description]
     */
    function getNextId (type) {
      if (counters.hasOwnProperty(type)){
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
      angular.forEach(row, function(component) {
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
        propertyValues: (widget.properties || []).reduce(function(props, property) {
          props[property.name] = {
            type: 'constant',
            value: (property.type === 'text' ? gettextCatalog.getString( property.defaultValue ) : property.defaultValue)
          };
          return props;
        }, {})
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
        $$id:  getNextId('component'),
        $$widget: widget,
        $$templateUrl: 'js/editor/workspace/component-template.html',
        $$propertiesTemplateUrl: 'js/editor/properties-panel/component-properties-template.html',
        $$parentContainerRow: parentRow
      });
    }



    function createContainer(parentRow) {
      var container = {
        type: 'container',
        dimension: resolutions.getDefaultDimension(),
        propertyValues: angular.extend(commonParams.getDefaultValues(), {
          'repeatedCollection': {
            type: 'constant',
            value: ''
          }
        }),
        rows: [
          []
        ]
      };

      service.initializeContainer(container, parentRow);
      return container;
    }

    function initializeContainer(container, parentRow) {
      angular.extend( container, {
        $$id: getNextId('container'),
        $$widget: widgetFactory.createContainerWidget(),
        $$templateUrl: 'js/editor/workspace/container-template.html',
        $$propertiesTemplateUrl: 'js/editor/properties-panel/container-properties-template.html',
        $$parentContainerRow: parentRow
      });

      container.rows.forEach(initializeRow.bind(null, container));
    }

    function createTabsContainer(parentRow) {
      var container = {
        type: 'tabsContainer',
        dimension:  resolutions.getDefaultDimension(),
        propertyValues: commonParams.getDefaultValues()
      };
      container.tabs = ['Tab 1', 'Tab 2'].map(createNewTab);

      service.initializeTabsContainer(container, parentRow);
      return container;
    }

    function initializeTabsContainer(container, parentRow) {
      angular.extend( container, {
        $$id: getNextId('tabsContainer'),
        $$widget: widgetFactory.createTabsContainerWidget(),
        $$templateUrl: 'js/editor/workspace/tabs-container-template.html',
        $$propertiesTemplateUrl: 'js/editor/properties-panel/component-properties-template.html',
        $$parentContainerRow: parentRow
      });

      container.tabs.forEach( function(tab) {
        tab.$$parentTabsContainer = container;
        service.initializeContainer(tab.container);
      });
    }

    /**
     * Creates a new tab for the given tabs container, with the given title
     */
    function createNewTab(title) {
      return {
        title: title,
        container: {
          type: 'container',
          propertyValues: commonParams.getDefaultValues(),
          rows: [
            []
          ]
        }
      };
    }

    function createFormContainer(parentRow) {
      var container = {
        type: 'formContainer',
        dimension: resolutions.getDefaultDimension(),
        propertyValues: angular.extend(commonParams.getDefaultValues(), {
          'url': {
            type: 'constant',
            value: ''
          },
          'method': {
            type: 'constant',
            value: ''
          }
        }),
        container: {
          type: 'container',
          propertyValues: commonParams.getDefaultValues(),
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
        $$widget: widgetFactory.createFormContainerWidget(),
        $$templateUrl: 'js/editor/workspace/form-container-template.html',
        $$propertiesTemplateUrl: 'js/editor/properties-panel/component-properties-template.html',
        $$parentContainerRow: parentRow
      });

      service.initializeContainer(formContainer.container);
    }


    function getPaletteContainers() {
      return [
        {
          sectionName: 'widgets',
          sectionOrder: 1,
          component: widgetFactory.createContainerWidget(),
          init: initializeContainer,
          create: createContainer
        }, {
          sectionName: 'widgets',
          sectionOrder: 1,
          component: widgetFactory.createTabsContainerWidget(),
          init: initializeTabsContainer,
          create: createTabsContainer
        }, {
          sectionName: 'widgets',
          sectionOrder: 1,
          component: widgetFactory.createFormContainerWidget(),
          init: initializeFormContainer,
          create: createFormContainer
        }
      ];
    }

    function paletteWrapper(name, order, component) {
      return {
        component: component,
        sectionName: name,
        sectionOrder: order,
        init: initializeWidget.bind(null, component),
        create: createWidget.bind(null, component)
      };
    }

    this.getNextId = getNextId;
    this.createNewTab = createNewTab;
    this.initializePage = initializePage;

    this.createWidget = createWidget;
    this.initializeWidget = initializeWidget;

    this.createContainer = createContainer;
    this.initializeContainer = initializeContainer;

    this.createTabsContainer = createTabsContainer;
    this.initializeTabsContainer = initializeTabsContainer;

    this.createFormContainer = createFormContainer;
    this.initializeFormContainer = initializeFormContainer;

    this.getPaletteContainers = getPaletteContainers;
    this.paletteWrapper = paletteWrapper;
  });
