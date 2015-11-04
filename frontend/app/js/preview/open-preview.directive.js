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
angular.module('bonitasoft.designer.preview')
  .directive('openPreview', function($window, $state, resolutions) {

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

        var stateName =  'designer.'  + (attr.openPreview || 'page') + '.preview';

        function clickHandler() {
          $window.open($state.href(stateName, {
            resolution: resolutions.selected().key
          }), '_blank', paramsPopup);
        }

        el.on('click', clickHandler);

        // remove click handler on destroy
        scope.$on('$destroy', function() {
          el.off('click', clickHandler);
        });
      }
    };
  });
