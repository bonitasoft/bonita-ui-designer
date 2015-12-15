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

  'use strict';

  let _$scope, _$modal;

  angular
    .module('bonitasoft.designer.home')
    .directive('uidCreateArtifact', () => ({
      scope: true,
      require: '^HomeCtrl',
      templateUrl: 'js/home/create/home-create.html',
      controller: CreateArtifactCtrl,
      bindToController: true,
      controllerAs: 'create'
    }));

  class CreateArtifactCtrl {
    constructor($scope, $modal) {
      _$scope = $scope;
      _$modal = $modal;
    }

    createElement() {
      _$modal.open({
        templateUrl: 'js/home/create/create-popup.html',
        controller: 'CreatePopupController',
        controllerAs: 'createCtrl',
        size: 'sm',
        resolve: {
          artifacts: () => _$scope.artifacts
        }
      });
    }
  }
})();
