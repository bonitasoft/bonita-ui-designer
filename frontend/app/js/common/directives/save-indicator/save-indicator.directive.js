(() => {

  class SaveIndicator {

    constructor($timeout, $scope) {
      this.$timeout = $timeout;

      $scope.$on('saved', () => this.displaySaveIndicator());
    }

    displaySaveIndicator() {
      this.isVisible = true;
      this.$timeout(() => {
        this.isVisible = false;
      }, 1000);
    }
  }

  angular
    .module('bonitasoft.designer.common.directives')
    .directive('saveIndicator', () => ({
      controller: SaveIndicator,
      controllerAs: 'indicator',
      templateUrl: 'js/common/directives/save-indicator/save-indicator.html'
    }));
})();
