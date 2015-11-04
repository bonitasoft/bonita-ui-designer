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
(function() {

  angular
    .module('bonitasoft.designer.common.directives')
    .directive('alertContent', alertContent);

  /**
   * Directive that display alert content
   * alert should be an object with following attributes
   *  - content: html content displayed in alert
   *  or
   *  - contentUrl: url of template that will be used to be displayed in alert
   *  - context: context for interpolation (will extends directive scope)
   */
  function alertContent($compile, $templateCache) {
    return function(scope, element, attrs) {
      scope.$watch(
        function(scope) {
          return scope.$eval(attrs.alertContent);
        },
        function(alert) {
          if (alert.contentUrl) {
            alert.content = $templateCache.get(alert.contentUrl);
          }
          element.html(alert.content);
          $compile(element.contents())(angular.extend(scope, alert.context));
        }
      );
    };
  }

})();
