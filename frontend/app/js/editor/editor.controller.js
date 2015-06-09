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

angular.module('pb.controllers').controller('EditorCtrl', function($scope, $state, $stateParams, $window, artifactRepo, paletteService, resolutions, artifact, mode, arrays, componentUtils, commonParams, componentFactory, keymaster, $modal, utils) {

  'use strict';

  /**
   * Names the current browsing context
   */
  $window.name = 'editor';

  /**
   * The editor mode
   */
  $scope.mode = mode || 'page';
  /**
   * The root container of the editor, always present. If there is no page loaded, then we build an empty one.
   * It initially contains a single row, and may not contain less than a row. The counters are used by e2e tests.
   */
  $scope.page = artifact;

  $scope.resolution = function() {
    return resolutions.selected();
  };

  /**
   * Adding DEL keyboard shortcut
   */
  keymaster('del', function(){
    if ( !$scope.currentComponent ) {
      return;
    }

    $scope.$apply(function() {
      $scope.removeCurrentComponent();
    });
  });

  keymaster('right', function(){
    moveSelection(+1);
  });

  keymaster('left', function(){
    moveSelection(-1);
  });

  keymaster('ctrl+s', function() {
    $scope.$apply(function() {
      $scope.save();
    });
    // prevent default browser action
    return false;
  });

  function moveSelection(offset) {
    var components = componentUtils.getVisibleComponents($scope.page);
    var index = components.indexOf($scope.currentComponent) + offset;

    index = utils.clamp(0, index, components.length - 1);

    $scope.$apply(function() {
      $scope.selectComponent(components[index]);
    });
  }

  $scope.$on('$destroy', function(){
    keymaster.unbind('del');
    keymaster.unbind('right');
    keymaster.unbind('left');
  });

  /**
   * Returns the CSS classes that must be set on a component in the editor to reflect its width and the currently
   * selected resolution. It returns an array containing the columnClass of the component, as well as
   * component-selected if the current selection is the given component.
   * Note that this function is called for a component of type 'widget' or of type 'container'.
   */
  $scope.componentClasses = function(component) {
    var result = [componentUtils.column.className(component)];

    return result;
  };

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
    $scope.currentTab = null;
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
  $scope.dropElement = function (element) {
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
    $scope.currentTab = null;
    $scope.currentComponent = component;
    if (event) {
      event.stopPropagation();
    }
  };

  $scope.deselectComponent = function() {

    $scope.currentContainerRow = null;
    $scope.currentTab = null;
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
    var newComponent = dragData.create($scope.currentContainerRow);
    arrays.insertAtPosition(newComponent, index, $scope.currentContainerRow.row);
    componentUtils.column.computeSizeItemInRow($scope.currentContainerRow.row);
    $scope.selectComponent(newComponent);
  };

  /**
   * Adds a new row before the currently selected one. A row must be selected before calling this function.
   *
   * TODO remove as not used anywhere but in test
   */
  $scope.addRowBeforeCurrent = function() {
    var rows = $scope.currentContainerRow.container.rows;
    var rowIndex = rows.indexOf($scope.currentContainerRow.row);
    rows.splice(rowIndex, 0, []);
  };

  /**
   * Adds a new row after the currently selected one. A row must be selected before calling this function.
   * TODO remove as not used anywhere but in test
   */
  $scope.addRowAfterCurrent = function() {
    var rows = $scope.currentContainerRow.container.rows;
    var rowIndex = rows.indexOf($scope.currentContainerRow.row);
    rows.splice(rowIndex + 1, 0, []);
  };

  /**
   * Removes the currently selected row from its container. A row must be selected before calling this function,
   * and it should not be the only one in its container, because a container must always contain at least one row.
   */
  $scope.removeCurrentRow = function() {
    var rows = $scope.currentContainerRow.container.rows;
    var rowIndex = rows.indexOf($scope.currentContainerRow.row);
    rows.splice(rowIndex, 1);
    $scope.currentContainerRow = null;
  };

  /**
   * Removes the currently selected component (widget or container) from its parent row. A component must be selected
   * before calling this function.
   * @todo refactor, it's the same code replaceCurrentComponent you can use third arg in splice to replace cf https://developer.mozilla.org/fr/docs/Web/JavaScript/Reference/Objets_globaux/Array/splice
   */
  $scope.removeCurrentComponent = function(item) {
    var component = $scope.currentComponent || item;
    var row = component.$$parentContainerRow.row;
    var componentIndex = row.indexOf(component);
    row.splice(componentIndex, 1);
    $scope.currentComponent = null;
  };


  /**
   * Function called when we want the current selection to be a tab. Selecting a tab allows displaying the editor
   * for this tab, and the controls used to move the tab.
   * Selecting a tab automatically unselects the previously selected row or component or tab.
   * @param tab the tab to select
   * @param event the event, used to prevent the click to propagate to parent elements
   */
  $scope.selectTab = function(tab, event) {
    $scope.currentContainerRow = null;
    $scope.currentComponent = null;
    $scope.currentTab = tab;

    if (event) {
      event.stopPropagation();
    }
  };

  /**
   * Adds a tab before the current tab
   */
  $scope.addTabBeforeCurrent = function() {
    var tab = $scope.currentTab;
    var tabs = tab.$$parentTabsContainer.tabs;
    var newTab = componentFactory.createNewTab('Tab ' + (tabs.length + 1));
    newTab.$$parentTabsContainer = tab.$$parentTabsContainer;
    tabs.splice(tabs.indexOf(tab), 0, newTab);
  };

  /**
   * Adds a tab after the current tab
   */
  $scope.addTabAfterCurrent = function() {
    var tab = $scope.currentTab;
    var tabs = tab.$$parentTabsContainer.tabs;
    var newTab = componentFactory.createNewTab('Tab ' + (tabs.length + 1));
    newTab.$$parentTabsContainer = tab.$$parentTabsContainer;
    tabs.splice(tabs.indexOf(tab) + 1, 0, newTab);
  };

  /**
   * Removes the current tab
   */
  $scope.removeCurrentTab = function() {
    var tab = $scope.currentTab;
    var tabsContainer = tab.$$parentTabsContainer;
    var tabs = tabsContainer.tabs;
    var index = tabs.indexOf(tab);
    tabs.splice(index, 1);
    if (index >= tabs.length) {
      index--;
    }
    tab.$$parentTabsContainer.$$openedTab = tabs[index];
    $scope.currentTab = null;
  };

  /**
   * Tells if the given tab is the currently selected one.
   */
  $scope.isCurrentTab = function(tab) {
    return !!$scope.currentTab && $scope.currentTab === tab;
  };

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

  $scope.save = function() {
    return artifactRepo.save($scope.page.id, $scope.page);
  };

  $scope.saveAs = function(page) {
    var modalInstance = $modal.open({
      templateUrl: 'js/editor/save-as-popup.html',
      backdrop: 'static',
      controller: function($scope, $modalInstance, page) {
        $scope.page = page;
        $scope.newName = page.name;

        $scope.ok = function() {
          $scope.page.name = $scope.newName;
          $modalInstance.close($scope.page);
        };

        $scope.cancel = function() {
          $modalInstance.dismiss('cancel');
        };
      },
      resolve: {
        page: function () {
          return page;
        }
      }
    });
    modalInstance.result
      .then(artifactRepo.create)
      .then(function (data) {
        $stateParams.id = data.id;
        $state.go($state.current, $stateParams, {
          reload: true
        });
      });
  };

  $scope.saveAndEditCustomWidget = function(widgetId) {
    artifactRepo.save($scope.page.id, $scope.page)
      .then(function() {
        $state.go('designer.widget', {
          widgetId: widgetId
        });
      });
  };

  $scope.saveAndExport = function() {
    artifactRepo.save($scope.page.id, $scope.page)
      .then(function() {
        $window.location = artifactRepo.exportUrl($scope.page);
      });
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
    if (!componentUtils.isEmpty( $scope.page)) {
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

  $scope.openHelp = function() {
    $modal.open({
      templateUrl: 'js/editor/help-popup.html',
      backdrop: 'static',
      size: 'lg',
      controller: function($scope, $modalInstance) {
        $scope.cancel = function() {
          $modalInstance.dismiss('cancel');
        };
      }
    });
  };

  /**
   * Object containing methods helpful for the component and container directives of the editor, and which is passed
   * as an attribute of these directives.
   */
  $scope.editor = {
    addComponentToRow: $scope.addComponentToRow,
    selectTab: $scope.selectTab,
    selectRow: $scope.selectRow,
    selectComponent: $scope.selectComponent,
    deselectComponent: $scope.deselectComponent,
    dropElement: $scope.dropElement,
    componentClasses: $scope.componentClasses,
    removeCurrentRow: $scope.removeCurrentRow,
    removeCurrentComponent: $scope.removeCurrentComponent,
    rowSize: $scope.rowSize,
    isCurrentRow: $scope.isCurrentRow,
    isCurrentTab: $scope.isCurrentTab,
    isCurrentComponent: $scope.isCurrentComponent,
    moveAtPosition: $scope.moveAtPosition,
    changeComponentWidth: $scope.changeComponentWidth,
    getComponentWidth: $scope.getComponentWidth
  };
});
