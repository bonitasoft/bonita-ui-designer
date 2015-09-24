(function () {

  'use strict';

  angular
      .module('bonitasoft.designer.editor.whiteboard')
      .service('whiteboardService', whiteboardService);

  function whiteboardService($timeout) {
    var onWidgetRemoveFunctions = [];
    var onWidgetAddFunctions = [];

    return {
      registerOnWidgetRemoveFunction: registerOnWidgetRemoveFunction,
      registerOnWidgetAddFunction: registerOnWidgetAddFunction,
      triggerRowRemoved: onRemoveRow,
      onAddWidget: onAddWidget,
      onRemoveWidget: onRemoveWidget,
      onRemoveContainer: onRemoveContainer,
      onRemoveTabsContainer: onRemoveTabsContainer,
      onRemoveTab: onRemoveTab,
      onRemoveFormContainer: onRemoveFormContainer
    };

    function registerOnWidgetRemoveFunction(fn) {
      onWidgetRemoveFunctions.push(fn);
    }

    function registerOnWidgetAddFunction(fn) {
      onWidgetAddFunctions.push(fn);
    }

    function onRemoveWidget(widget) {
      executeFunctionsForComponent(onWidgetRemoveFunctions, widget);
    }

    function onAddWidget(widget) {
      executeFunctionsForComponent(onWidgetAddFunctions, widget);
    }

    /**
     * Execute each function of functions array for component.
     * Functions are executed in $timeout to be non blocking for UI
     */
    function executeFunctionsForComponent(functions, component) {
      functions.forEach(function (fn) {
        $timeout(fn.bind(null, component), 0);
      });
    }

    function onRemoveRow(row) {
      angular.forEach(row, function (component) {
        component.triggerRemoved();
      });
    }

    function onRemoveContainer(container) {
      container.rows.forEach(onRemoveRow);
    }

    function onRemoveTabsContainer(tabsContainer) {
      tabsContainer.tabs.forEach(onRemoveTab);
    }

    function onRemoveTab(tab) {
      onRemoveContainer(tab.container);
    }

    function onRemoveFormContainer(formContainer) {
      onRemoveContainer(formContainer.container);
    }
  }
})();
