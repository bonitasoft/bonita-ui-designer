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
class ResolutionBarCtrl {

  constructor($scope, resolutions, $stateParams) {
    this.resolutionsService = resolutions;
    this.onChange = $scope.onChange;
    $scope.$watchCollection(() => resolutions.all(), newResolutions => this.resolutions = newResolutions);
    $scope.$watch(() => resolutions.selected(), selected => this.currentResolution = selected);
    $scope.$watch(() => $stateParams.resolution, value => this.update({ key: value }));
  }

  update(resolution) {
    this.currentResolution = this.resolutionsService.select(resolution.key);
    if (this.onChange) {
      this.onChange(resolution);
    }
  }
}

(() => angular.module('bonitasoft.designer.resolution')
  .directive('resolutionsBar', () => ({
      scope: {
        onChange: '='
      },
      templateUrl: 'js/resolutions/resolutions-bar.html',
      controllerAs: 'vm',
      controller: ResolutionBarCtrl
    })
  ))();
