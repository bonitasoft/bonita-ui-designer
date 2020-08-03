/*******************************************************************************
 * Copyright (C) 2009, 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
