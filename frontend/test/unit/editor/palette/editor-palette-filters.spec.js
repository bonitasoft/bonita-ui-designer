(function() {
  'use strict';

  describe('filterByComponentName filter', function() {
    let $filter, widgets;

    beforeEach(angular.mock.module('bonitasoft.designer.editor.palette'));

    beforeEach(inject(function(_$filter_) {
      $filter = _$filter_;

      widgets = [
        {
          component: {
            name: 'Container'
          }
        },
        {
          component: {
            name: 'Form Container'
          }
        }
      ];
    }));

    it('should return all widgets when no search value', function() {
      expect($filter('filterByComponentName')(widgets, undefined)).toEqual(widgets);
      expect($filter('filterByComponentName')(widgets, '')).toEqual(widgets);
    });

    it('should return no widgets when search value is not in a widget component name', function() {
      expect($filter('filterByComponentName')(widgets, 'zzz')).toEqual([]);
    });


    it('should return one widget when the search value is Form', function() {
      let widgetsFiltered = [
        {
          component: {
            name: 'Form Container'
          }
        }
      ];

      expect($filter('filterByComponentName')(widgets, 'Form')).toEqual(widgetsFiltered);
      expect($filter('filterByComponentName')(widgets, 'form')).toEqual(widgetsFiltered);
      expect($filter('filterByComponentName')(widgets, 'FOrm')).toEqual(widgetsFiltered);
    });
  });

})();
