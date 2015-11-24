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
angular.module('bonitasoft.designer.home')
  .directive('fileDownload', function($document, $http) {
    'use strict';

    $document.find('body').append('<iframe class="ExportArtifact" src=""></iframe>');
    var iframe = $document[0].querySelector('.ExportArtifact');

    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        element.on('click', function() {
          //We need to intercept error when we change the iframe src
          $http
            .get(attrs.href)
            .success(function() {
              iframe.setAttribute('src', attrs.href);
            });
        });
        scope.$on('$destroy', () => element.off('click'));
      }
    };
  });
