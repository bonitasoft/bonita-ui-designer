angular.module('bonitasoft.designer.editor.palette')
  .filter('filterByComponentName', function() {
    return function(widgets, search) {
      // Filter the widgets that should not appear in the Palette
      widgets = widgets.filter(widget => {
        return !widget.component || widget.component && widget.component.id !== 'pbTabContainer';
      });
      if (!search) {
        return widgets;
      }
      return widgets.filter(widget => {
        return widget.component.name.toLowerCase().indexOf(search.toLowerCase()) >= 0;
      });
    };
  });
