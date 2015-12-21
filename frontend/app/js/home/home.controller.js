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
 * The home page controller, listing the existing pages, widgets
 */
angular.module('bonitasoft.designer.home').controller('HomeCtrl', function($scope, $modal, artifactStore, artifactFactories) {

  /**
   * When something is deleted, we need to refresh every collection,
   * because we can maybe delete a component we couldn't previously
   * example :
   *   custom widget <hello> was used in page <person>, so we could not delete it.
   *   if <hello> is deleted, now we can delete <person>
   * @returns {Promise}
   */
  $scope.refreshAll = () => artifactStore.load()
    .then((artifacts) => $scope.artifacts = artifacts);

  $scope.openHelp = function() {
    $modal.open({
      templateUrl: 'js/home/help-popup.html',
      size: 'lg',
      controller: function($scope, $modalInstance) {
        $scope.cancel = function() {
          $modalInstance.dismiss('cancel');
        };
      }
    });
  };

  $scope.factories = artifactFactories.getFactories();

  $scope.search = '';
  $scope.refreshAll();
});
