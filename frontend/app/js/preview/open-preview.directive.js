angular.module('pb.preview')
  .directive('openPreview', function ($window, $state, resolutions) {

    'use strict';

    /**
     * Open a popup with the preview of your page
     * You can set a custom width|height
     *    - popup-width="1024" // default
     *    - popup-height="768" // default
     */
    return {
      link: function(scope, el, attr) {

        // With IE10 we need to set a size and resizable
        var params = 'width=pWidth,height=pHeight,resizable=1,scrollbars=1';
        var paramsPopup = params
                            .replace(/pWidth/, attr.popupWidth || 1024)
                            .replace(/pHeight/, attr.popupHeight || 768);

        var stateName =  'designer.'  + (attr.openPreview || 'page' ) + '.preview';

        function clickHandler() {
          $window.open($state.href(stateName, {
            resolution: resolutions.selected().key
          }), '_blank', paramsPopup);
        }

        el.on('click', clickHandler);

        // remove click handler on destroy
        scope.$on('$destroy', function(){
          el.off('click', clickHandler);
        });
      }
    };
  });
