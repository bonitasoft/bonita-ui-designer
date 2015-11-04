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
 * Listen to an input file and assign the selected file to a scope variable
 */
(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.common.directives')
    .directive('fileInputChange', fileInputChange);

  function fileInputChange() {

    return {
      require: 'ngModel',
      link: function(scope, elem, attr, ngModel) {

        function update(event) {
          var filename = '';
          if (event.target.files && event.target.files.length > 0) {
            filename = event.target.files[0].name;
          } else {
            filename = event.target.value.match(/([^\\|\/]*)$/)[0];
          }

          scope.$apply(function() {
            ngModel.$setViewValue(filename);
            ngModel.$render();
          });
        }

        elem.on('change', update);

        scope.$on('$destroy', function() {
          elem.off('change', update);
        });

      }
    };
  }
})();
