(function() {

  'use strict';

  angular
      .module('bonitasoft.designer.editor.whiteboard')
      .service('whiteboardService', whiteboardService);

  function whiteboardService($timeout, arrays) {
    var onWidgetRemoveFunctions = [];
    var onWidgetAddFunctions = [];

    var widgetIds = [];

    return {
      registerOnWidgetRemoveFunction: registerOnWidgetRemoveFunction,
      registerOnWidgetAddFunction: registerOnWidgetAddFunction,
      triggerRowRemoved: onRemoveRow,
      triggerInitWidget: onInitWidget,
      onAddWidget: onAddWidget,
      onRemoveWidget: onRemoveWidget,
      onRemoveContainer: onRemoveContainer,
      onRemoveTabsContainer: onRemoveTabsContainer,
      onRemoveTab: onRemoveTab,
      onRemoveFormContainer: onRemoveFormContainer,
      contains,
      reset
    };

    function reset() {
      widgetIds = [];
    }

    function contains(widget) {
      return widgetIds.indexOf(widget.id) > -1;
    }

    function registerOnWidgetRemoveFunction(fn) {
      onWidgetRemoveFunctions.push(fn);
    }

    function registerOnWidgetAddFunction(fn) {
      onWidgetAddFunctions.push(fn);
    }

    function onRemoveWidget(widget) {
      arrays.removeFirst(widget.id, widgetIds);
      executeFunctionsForComponent(onWidgetRemoveFunctions, widget);
    }

    function onAddWidget(widget) {
      executeFunctionsForComponent(onWidgetAddFunctions, widget);
    }

    function onInitWidget(widget) {
      widgetIds.push(widget.id);
    }

    /**
     * Execute each function of functions array for component.
     * Functions are executed in $timeout to be non blocking for UI
     */
    function executeFunctionsForComponent(functions, component) {
      functions.forEach(function(fn) {
        $timeout(fn.bind(null, component), 0);
      });
    }

    function onRemoveRow(row) {
      angular.forEach(row, function(component) {
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
