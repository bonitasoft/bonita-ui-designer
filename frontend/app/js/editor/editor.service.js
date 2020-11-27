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
(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.editor')
    .service('editorService', editorService);

  function editorService($q, widgetRepo, fragmentRepo, fragmentService, components, whiteboardComponentWrapper, pageElementFactory, properties, alerts,
                         gettext, whiteboardService, assetsService, dataManagementRepo, $uibModal, gettextCatalog, migration) {

    var paletteItems = {};
    var page;

    return {
      addPalette: addPalette,
      initialize: initialize,
      addWidgetAssetsToPage,
      removeAssetsFromPage,
      createWidgetWrapper: createWidgetWrapper
    };

    function addPalette(key, repository) {
      paletteItems[key] = repository;
    }

    // function loadNewWidgets(isExperimental){
    //   let newWidgets = $q.defer();
    //   let widgets = [];
    //   if(isExperimental){
    //     widgets.push({id:'pbInput',type:'widget', name:'Input', custom:false});
    //     widgets.push({id:'pbText',type:'widget', name:'Text', custom:false});
    //     widgets.push({id:'pbDate',type:'widget', name:'Date', custom:false});
    //     widgets.push({id:'customDateWC',type:'widget', name:'customDate', custom:true});
    //     newWidgets.resolve(widgets);
    //   }else{
    //     newWidgets.resolve([]);
    //   }
    //   return newWidgets.promise;
    // }

    function initialize(repo, id, isWCWidgets) {
      let allWidgets = widgetRepo.allWCWidgets(isWCWidgets);
      let dataWidgets = dataManagementRepo.getDataObjects()
        .then(addDataManagement);

      //let allWidgets;
      // if(!isExperimental){
      //   allWidgets = widgetRepo.all();
      // } else {
      //   allWidgets = loadNewWidgets(isExperimental);
      // }

      return $q.all([dataWidgets, allWidgets])
        .then(values => {
          let widgets = [];
          values.forEach((element) => {
            widgets = widgets.concat(element);
          });
          return widgets;
        })
        .then(initializePalette)
        //Retrieve and register fragments that can be dropped without cyclic dependencies
        .then(() => fragmentRepo.allNotUsingElement(id))
        .then((fragmentResponse) => fragmentService.register(fragmentResponse.data))
        .then(() => repo.migrationStatus(id))
        .then((response) => {
          return migration.handleMigrationStatus(id, response.data).then(() => {
            if (response.data.migration) {
              repo.migrate(id).then(response => migration.handleMigrationNotif(id, response.data));
            }
          });
        })
        .then(() => repo.load(id))
        .catch((error) => {
          if (error.message && error.status !== 422) {
            alerts.addError(error.message);
          }
          return $q.reject(error);
        })
        .then((response) => {
          whiteboardService.reset();
          page = response.data;
          whiteboardComponentWrapper.wrapPage(page);
          return page;
        });
    }

    function initializePalette(items) {
      function filterCustomWidgets(val, item) {
        return item.type === 'widget' && item.custom === val && (!item.status || item.status.compatible);
      }

      var coreWidgets = items.filter(filterCustomWidgets.bind(null, false))
        .map(paletteWidgetWrapper.bind(null, gettext('widgets'), 1));
      console.log('coreWidgets', coreWidgets);
      var customWidgets = items.filter(filterCustomWidgets.bind(null, true))
        .map(paletteWidgetWrapper.bind(null, gettext('custom widgets'), 2));

      var containers = items.filter((widget) => widget.type === 'container')
        .map(paletteContainerWrapper);

      // var newWidgets = items.filter(filterCustomWidgets.bind(null, false))
      //   .map(paletteWebComponentWrapper.bind(null, gettext('widgets'), 1));

      var dataManagement = items.filter((widget) => widget.type === 'model')
        .map(paletteDataManagementWrapper.bind(null, gettext('data model'), 0));

      // reset the components map
      components.reset();
      components.register(containers);
      components.register(coreWidgets);
      //components.register(newWidgets);
      components.register(customWidgets);
      components.register(dataManagement);
    }

    function addDataManagement(data) {
      let dataManagementWidgets = [];
      data.objects.forEach(obj => {
        dataManagementWidgets.push({
          id: obj.id,
          name: obj.name,
          description: obj.description,
          type: 'model',
        });
      });

      return dataManagementWidgets;
    }

    function createWidgetWrapper(component) {
      let extended = properties.addCommonPropertiesTo(component);
      return {
        component: extended,
        create: createWidget.bind(null, extended)
      };
    }

    function paletteWidgetWrapper(name, order, component) {
      var extended = properties.addCommonPropertiesTo(component);
      return {
        component: extended,
        sectionName: name,
        sectionOrder: order,
        init: whiteboardComponentWrapper.wrapWidget.bind(null, extended),
        create: createWidget.bind(null, extended)
      };
    }

    // function paletteWebComponentWrapper(name, order, component) {
    //   return {
    //     component: component,
    //     sectionName: name,
    //     sectionOrder: order
    //   };
    // }

    function paletteDataManagementWrapper(name, order, component) {
      return {
        component: component,
        sectionName: name,
        sectionOrder: order
      };
    }

    function paletteContainerWrapper(component) {
      let fns = {
        pbContainer: {
          init: whiteboardComponentWrapper.wrapContainer,
          create: createContainer
        },
        pbTabsContainer: {
          init: whiteboardComponentWrapper.wrapTabsContainer,
          create: createTabsContainer
        },
        pbTabContainer: {
          init: whiteboardComponentWrapper.wrapTabContainer,
          create: createTabContainer
        },
        pbFormContainer: {
          init: whiteboardComponentWrapper.wrapFormContainer,
          create: createFormContainer
        },
        pbModalContainer: {
          init: whiteboardComponentWrapper.wrapModalContainer,
          create: createModalContainer
        }
      };

      var extended;

      if (component.id === 'pbModalContainer') {
        extended = properties.addCssPropertyTo(component);
      } else {
        extended = properties.addCommonPropertiesTo(component);
      }

      return {
        component: extended,
        sectionName: gettext('widgets'),
        sectionOrder: 1,
        init: fns[extended.id].init.bind(null, extended),
        create: fns[extended.id].create.bind(null, extended)
      };
    }

    function createWidget(widget, parentRow) {
      var element = pageElementFactory.createWidgetElement(widget);
      return whiteboardComponentWrapper.wrapWidget(widget, element, parentRow);
    }

    function createContainer(container, parentRow) {
      var element = pageElementFactory.createContainerElement(container);
      return whiteboardComponentWrapper.wrapContainer(container, element, parentRow);
    }

    function createTabsContainer(tabsContainer, parentRow) {
      var element = pageElementFactory.createTabsContainerElement(tabsContainer);
      return whiteboardComponentWrapper.wrapTabsContainer(tabsContainer, element, parentRow);
    }

    function createTabContainer(tabContainer, parentRow) {
      var element = pageElementFactory.createTabContainerElement(tabContainer);
      return whiteboardComponentWrapper.wrapTabContainer(tabContainer, element, parentRow);
    }

    function createFormContainer(formContainer, parentRow) {
      var element = pageElementFactory.createFormContainerElement(formContainer);
      return whiteboardComponentWrapper.wrapFormContainer(formContainer, element, parentRow);
    }

    function createModalContainer(modalContainer, parentRow) {
      var element = pageElementFactory.createModalContainerElement(modalContainer);
      return whiteboardComponentWrapper.wrapModalContainer(modalContainer, element, parentRow);
    }

    function addWidgetAssetsToPage(widget) {
      assetsService.addWidgetAssetsToPage(widget, page);
    }

    function removeAssetsFromPage(widget) {
      if (!whiteboardService.contains(widget)) {
        assetsService.removeAssetsFromPage(widget, page);
      }
    }
  }
})
();
