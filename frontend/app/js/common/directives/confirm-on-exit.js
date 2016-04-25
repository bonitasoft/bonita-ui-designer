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
angular.module('bonitasoft.designer.common.directives')
  .directive('confirmOnExit', function($rootScope, $stateParams, $state, $injector, $window, gettextCatalog) {

    'use strict';

    var message = gettextCatalog.getString('You have unsaved changes. Do you really want to leave ?');

    return {
      scope: {
        'confirmData': '='
      },
      link: function($scope) {
        // get the service from resolve parameter
        if (!$state.current.resolve || !$state.current.resolve.artifactRepo) {
          return;
        }

        var repository = $injector.get($state.current.resolve.artifactRepo[0]);
        var stateName = $state.current.name;
        repository.initLastSavedState($scope.confirmData);
        var onRouteChangeOff = $rootScope.$on('$stateChangeStart', routeChangeHandler);

        $window.onbeforeunload = function() {
          if (repository.needSave($scope.confirmData)) {
            return message;
          }
        };

        function routeChangeHandler(event, newState) {
          if (newState.name.indexOf(stateName) !== -1) {
            // we don't care about internal state change
            return;
          }
          if (repository.needSave($scope.confirmData) && !window.confirm(message)) {
            event.preventDefault();
          }
        }

        // remove route event listener
        $scope.$on('$destroy', function() {
          $window.onbeforeunload = undefined;
          onRouteChangeOff();
        });
      }
    };
  });
