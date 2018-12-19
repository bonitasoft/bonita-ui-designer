angular.module('bonitasoft.designer.editor.palette')
  .filter('filterByComponentName', function() {
    return function(widgets, search) {
      if (!search) {
        return widgets;
      }
      return widgets.filter(widget => {
        return widget.component.name.toLowerCase().indexOf(search.toLowerCase()) >= 0;
      });
    };
  });
