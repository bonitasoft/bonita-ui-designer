
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
 * A wrapper container to ease communitation between splitter-toggle and
 * splitter-horizontal
 */

angular
  .module('bonitasoft.designer.common.directives')
  .controller('SplitterContainerCtrl', function($scope, $state) {
    var splitter;
    var isOpen = true;

    this.isActive = function(stateName) {
      return $state.current.name === stateName;
    };

    this.isOpen = function() {
      return isOpen;
    };

    this.register = function(controller) {
      splitter = controller;
    };

    this.toggle = function(stateName) {
      if (!isOpen) {
        splitter.openBottom();
        isOpen = true;
      } else if ($state.current.name === stateName) {
        splitter.closeBottom();
        isOpen = false;
      }

      $state.go(stateName, undefined, { location: false });
    };

    $scope.$on('$destroy', function() {
      splitter = undefined;
    });
  })
  .directive('splitterContainer', function() {
    return {
      controller: 'SplitterContainerCtrl',
      controllerAs: 'splitterContainer',
    };
  });
