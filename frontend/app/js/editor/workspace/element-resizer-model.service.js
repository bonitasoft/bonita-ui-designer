angular.module('pb.factories')
  .factory('elementResizerModel', function() {

    'use strict';

    return {
      isResisableComponent: false,

      set: function set(key, value) {
        this[key] = value;
      },
      isResizable: function isResizable(active) {
        this.isResisableComponent = !!active;
      },
      computeCols: function computeCols(resizeWidthInBootstrap) {
        this.bootstrapNewWidth = this.bootstrapWidth - resizeWidthInBootstrap;
      },
      resize: function resize(item, size) {
        if(this[item]) {
          this[item].style.width = size + 'px';
        }
      },
      toggleVisibility: function toggleVisibility(item) {
        var itemReverse = ('right' === item) ? 'left' : 'right';

        if(this[item] && this[itemReverse]) {
          this[item].style.visibility = 'visible';
          this[itemReverse].style.visibility = 'hidden';
        }
      }
    };
  });
