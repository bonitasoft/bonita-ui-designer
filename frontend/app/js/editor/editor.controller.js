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
/**
 * The editor controller. It handles the palette, the resolution changes, the selections, and provides
 * common functions to the directives used inside the page.
 */

angular.module('bonitasoft.designer.editor').controller('EditorCtrl', function($scope, $state, $stateParams, $window,
 artifactRepo, resolutions, artifact, mode, arrays, componentUtils, keyBindingService, $uibModal, utils, whiteboardService,
 $timeout, widgetRepo, editorService, gettextCatalog, dataManagementRepo, businessDataUpdateService, whiteboardComponentWrapper, uiGeneration) {

  'use strict';

  /**
   * Names the current browsing context
   */
  $window.name = 'editor';

  /**
   * The editor mode
   */
  $scope.mode = mode || 'page';

  // We delay page resolution to not block the ui while having  a big page to be displayed
  $timeout(() => {
    $scope.page = artifact;
    artifactRepo.setLastSavedState($scope.page);
  }, 0);

  $scope.resolution = function() {
    return resolutions.selected();
  };

  /**
   * Adding DEL keyboard shortcut
   */
  keyBindingService.bind('del', function() {
    if (!$scope.currentComponent) {
      return;
    }

    $scope.$apply(function() {
      $scope.removeCurrentComponent();
    });
  });

  keyBindingService.bind('right', function() {
    moveSelection(+1);
  });

  keyBindingService.bind('left', function() {
    moveSelection(-1);
  });

  function moveSelection(offset) {
    var components = componentUtils.getVisibleComponents($scope.page);
    var index = components.indexOf($scope.currentComponent) + offset;

    index = utils.clamp(0, index, components.length - 1);

    $scope.$apply(function() {
      $scope.selectComponent(components[index]);
    });
  }

  $scope.$on('$destroy', function() {
    keyBindingService.unbind(['del', 'right', 'left']);
  });

  $scope.componentClasses = componentUtils.getResolutionClasses;

  /**
   * Function called when we want the current selection to be a row of a container. Selecting a row of a container
   * allows displaying the palette, in order to add a widget or container component to the selected row, to delete
   * or move the selected row, or to add a new row before or after the selected row.
   * Selecting a row automatically unselects the previously selected row or component or tab.
   * @param container the container directly containing the row to select
   * @param row the row to select
   * @param event the event, used to prevent the click to propagate to parent elements
   */
  $scope.selectRow = function(container, row, event) {

    $scope.currentComponent = null;
    $scope.currentContainerRow = {
      row: row,
      container: container
    };
    if (event) {
      event.stopPropagation();
    }
  };

  /**
   * Drag and drop an item, it means re-attach this item
   * @param  {Object} element Widget already configured
   * @return {void}
   */
  $scope.dropElement = function(element) {
    $scope.currentComponent = element;
    element.$$parentContainerRow = $scope.currentContainerRow;
    $scope.currentContainerRow.row.push(element);
    componentUtils.column.computeSizeItemInRow($scope.currentContainerRow.row);
  };

  /**
   * move a component to a specific position inside the current active row
   * @param  {int}    componentIndex new index for component
   * @param  {Object} component      a component
   */
  $scope.moveAtPosition = function(componentIndex, component) {
    arrays.moveAtPosition(component, componentIndex, $scope.currentContainerRow.row);
    $scope.selectComponent(component);
  };

  /**
   * Function called when we want the current selection to be a widget component or a container component. Selecting
   * a widget component allows displaying the editor for this widget component, and the controls used to move the
   * component. Selecting a container allows displaying the container editor and the controls used to move the
   * container.
   * Selecting a component automatically unselects the previously selected row or component or tab.
   * @param component the component to select
   * @param event the event, used to prevent the click to propagate to parent elements
   */
  $scope.selectComponent = function(component, event) {

    $scope.currentContainerRow = null;
    $scope.currentComponent = component;
    if (event) {
      event.stopPropagation();
    }
  };

  $scope.deselectComponent = function() {

    $scope.currentContainerRow = null;
    $scope.currentComponent = null;
    if (event) {
      event.stopPropagation();
    }
  };

  /**
   * Function used to create a widget component for the given widget and add it to the currently selected container
   * row. A row must be selected before calling this function. Note that the created component keeps a reference to
   * its parent container row, in order to know how to move itself inside this row.
   * @param dragData {type: String, widget: Object}
   * @param index index of the element
   */
  $scope.addComponent = function(dragData, index) {
    if (dragData.component && dragData.component.type === 'model') {
      triggeredDataManagement(dragData.component, index);
    } else {
      var newComponent = dragData.create($scope.currentContainerRow);
      arrays.insertAtPosition(newComponent, index, $scope.currentContainerRow.row);
      componentUtils.column.computeSizeItemInRow($scope.currentContainerRow.row);
      $scope.selectComponent(newComponent);
      newComponent.triggerAdded();
    }
  };

  function removeRow(container, row) {
    var rows = container.rows;
    if (rows.length > 1) {
      var rowIndex = rows.indexOf(row);
      rows.splice(rowIndex, 1);
      whiteboardService.triggerRowRemoved(row);
    }
  }

  function triggeredDataManagement(dataComponent, index) {
    // Call dataRepo to get Query for this dataComponent Name
    $scope.queries = dataManagementRepo.getQueries(dataComponent.id);
    $uibModal.open({
      templateUrl: 'js/editor/bottom-panel/data-panel/data-management-filter-popup.html',
      controller: 'DataManagementPopupController',
      controllerAs: 'ctrl',
      resolve: {
        businessData: () => dataComponent,
        queriesForObject: () => $scope.queries,
        pageData: () => $scope.page.variables,
        businessDataUpdateService: () => businessDataUpdateService
      }
    }).result.then((data) => {
      if (data) {
        let dataObject = dataManagementRepo.getDataObject(dataComponent.id);
        if (dataObject.businessObject) {
          addDataManagementGeneratedUI(dataObject.businessObject, index, data.variable);
        }
      } else {
        if ($scope.currentContainerRow.row.length === 0) {
          // Remove row to don't let empty row
          removeRow($scope.currentContainerRow.container, $scope.currentContainerRow.row);
        }
      }
    });
  }

  function addDataManagementGeneratedUI(businessObject, index, varName) {
    // Needed to get information on pbContainer to wrap our generated content
    widgetRepo.load('pbContainer').then(res => {
      let containerRef = res.data;
      uiGeneration.generateUI(businessObject, varName).then(res => {
        if (!res.error) {
          let container = res.element.container;
          let row = $scope.currentContainerRow;
          whiteboardComponentWrapper.wrapContainer(containerRef, container, row);
          arrays.insertAtPosition(container, index, row.row);
          componentUtils.column.computeSizeItemInRow(row.row);
          $scope.selectComponent(container);
          container.triggerAdded();
          // generate Variable
          let allVariable = res.element.businessObjectVariable;
          Object.keys(allVariable).forEach(newVar => {
            $scope.page.variables[newVar] = allVariable[newVar];
          });
        }
      });
    });
  }

  /**
   * Removes the currently selected row from its container. A row must be selected before calling this function,
   * and it should not be the only one in its container, because a container must always contain at least one row.
   */
  $scope.removeCurrentRow = function() {
    removeRow($scope.currentContainerRow.container, $scope.currentContainerRow.row);
    $scope.currentContainerRow = null;
  };

  /**
   * Removes the currently selected component (widget or container) from its parent row. A component must be selected
   * before calling this function.
   * If a destinationRow is given, it means the component is moved so current row may not have to be deleted
   */
  $scope.removeCurrentComponent = function(item, destinationRow) {
    var component = $scope.currentComponent || item;

    var currentRow = component.$$parentContainerRow.row;
    var componentIndex = currentRow.indexOf(component);
    currentRow.splice(componentIndex, 1);
    if (currentRow.length === 0 && destinationRow !== currentRow) {
      removeRowAfterDrop(component.$$parentContainerRow.container, currentRow);
    }
    if (!destinationRow) {
      component.triggerRemoved();
    }
    $scope.currentComponent = null;
  };

  /**
   *
   * @returns {boolean}
   */
  function isNotSupportedBrowsers() {
    let ua = navigator.userAgent;
    /* MSIE used to detect old browsers and Trident used to newer ones*/
    return ua.indexOf('MSIE ') > -1 || ua.indexOf('Trident/') > -1 || ua.indexOf('Edge/') > -1;
  }

  $scope.switchCurrentComponent = function(item) {
    if (isNotSupportedBrowsers()) {
      $uibModal.open({
        templateUrl: 'js/editor/whiteboard/switchComponent/not-supported-browser-popup.html',
        size: 'small',
        backdrop: 'true'
      });
      return;
    }
    //Target are used in web-component but this keys doesn't exist. We need to add this key like this
    gettextCatalog.getString('Target');
    let component = $scope.currentComponent || item;
    let modalInstance = $uibModal.open({
      templateUrl: 'js/editor/whiteboard/switchComponent/switch-component-popup.html',
      controller: 'SwitchComponentPopupController',
      controllerAs: 'ctrl',
      size: 'lg',
      resolve: {
        widgets: widgetRepo.all().then(items => items),
        widgetFrom: angular.copy(component),
        dictionary: gettextCatalog.strings
      }
    });

    modalInstance.result.then(switchComponent.bind(null, component));
  };

  function switchComponent(component, resultData) {
    let widgetTo = JSON.parse(resultData.mapping);
    let row = component.$$parentContainerRow.row;
    let index = row.findIndex(p => p.$$hashKey === component.$$hashKey);

    widgetRepo.load(widgetTo.id).then(response => {
      let newWidget = response.data;
      $scope.removeCurrentComponent(component, row);
      let compo = editorService.createWidgetWrapper(newWidget);
      let newComponent = compo.create(component.$$parentContainerRow);

      Object.keys(newComponent.propertyValues).forEach(p => {
        newComponent.propertyValues[p] = widgetTo.options[p];
      });

      newComponent.dimension = resultData.dimension;
      arrays.insertAtPosition(newComponent, index, row);
      $scope.selectComponent(newComponent);
      newComponent.triggerAdded();
    });
  }

  /**
   * Remove the row at the end of the current digest loop
   * to avoid messing up row indexes during drag and drop.
   *
   * @param container
   * @param row
   */
  function removeRowAfterDrop(container, row) {
    $timeout(function() {
      removeRow(container, row);
    }, 0);
  }

  /**
   * Tells if the given row is the currently selected one.
   */
  $scope.isCurrentRow = function(row) {
    return !!$scope.currentContainerRow && $scope.currentContainerRow.row === row;
  };

  /**
   * Tells if the given component (widget or container) is the currently selected one.
   */
  $scope.isCurrentComponent = function(component) {
    return !!$scope.currentComponent && $scope.currentComponent === component;
  };

  /**
   * Computes the size of a row by summing its components size.
   * @param row the row to measure
   * @returns {number} the size
   */
  $scope.rowSize = function(row) {
    var size = 0;
    angular.forEach(row, function(component) {
      size += componentUtils.column.width(component);
    });
    return size;
  };

  $scope.saveAndEditCustomWidget = function(widgetId) {
    artifactRepo.save($scope.page)
      .then(function() {
        $state.go('designer.widget', {
          id: widgetId
        });
      });
  };

  $scope.save = function() {
    return artifactRepo.save($scope.page);
  };

  $scope.canBeSaved = function(page) {
    return !!page.name;
  };

  /**
   * Create a nex row at the bottom and add a component
   * @param  {Object} data  a component descriptor to add
   */
  $scope.appendComponent = function(event, data) {
    // we prevent from dropping existing widget
    if (data.$$widget) {
      return;
    }
    if (!componentUtils.isEmpty($scope.page)) {
      $scope.page.rows.push([]);
    }
    var lastRow = $scope.page.rows[$scope.page.rows.length - 1];
    $scope.addComponentToRow(data, $scope.page, lastRow, 0);
  };

  /**
   * Add a component to a row in a container
   * @param {Object} data      Component descriptor
   * @param {Object} container Container that hold the row
   * @param {Array} row        the row where append the component
   * @param {int} index        the index at where we insert the component
   */
  $scope.addComponentToRow = function(data, container, row, index) {

    $scope.editor.selectRow(container, row);
    $scope.addComponent(data, index);

  };

  $scope.resizePaletteHandler = function(isClosed) {
    $scope.isPaletteClosed = isClosed;
  };

  $scope.isPropertyPanelClosed = false;

  function togglePropertyPanel() {
    $scope.isPropertyPanelClosed = !$scope.isPropertyPanelClosed;
  }

  /**
   * Object containing methods helpful for the component and container directives of the editor, and which is passed
   * as an attribute of these directives.
   */
  $scope.editor = {
    addComponentToRow: $scope.addComponentToRow,
    selectRow: $scope.selectRow,
    selectComponent: $scope.selectComponent,
    deselectComponent: $scope.deselectComponent,
    dropElement: $scope.dropElement,
    componentClasses: $scope.componentClasses,
    removeCurrentRow: $scope.removeCurrentRow,
    removeCurrentComponent: $scope.removeCurrentComponent,
    switchCurrentComponent: $scope.switchCurrentComponent,
    rowSize: $scope.rowSize,
    isCurrentRow: $scope.isCurrentRow,
    isCurrentComponent: $scope.isCurrentComponent,
    moveAtPosition: $scope.moveAtPosition,
    changeComponentWidth: $scope.changeComponentWidth,
    getComponentWidth: $scope.getComponentWidth,
    page: $scope.page,
    togglePropertyPanel
  };

  $scope.widgetHelpPopover = {
    content: '',
    isOpen: false
  };

  var unregisterWidget = $scope.$watch(
    'currentComponent.$$widget',
    function() {
      $scope.widgetHelpPopover.isOpen = false;
    }
  );
  $scope.$on('$destroy', unregisterWidget);

  $scope.widgetHelpTemplateURL = 'js/editor/properties-panel/widgetHelpTemplate.html';

  $scope.triggerWidgetHelp = function() {
    if ($scope.widgetHelpPopover.isOpen) {
      $scope.widgetHelpPopover.isOpen = false;
    } else {
      $scope.widgetHelpPopover.isOpen = true;
      widgetRepo.getHelp($scope.currentComponent.$$widget.id).then(
        function(response) {
          $scope.widgetHelpPopover.content = response.data;
        }
      );
    }
  };

});
