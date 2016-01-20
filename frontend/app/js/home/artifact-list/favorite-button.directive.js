(function() {

  'use strict';

  angular
      .module('bonitasoft.designer.home')
      .directive('favoriteButton', favoriteButton);

  function favoriteButton() {
    return {
      scope: {
        artifact: '=',
        repository: '=artifactRepository'
      },
      controller: FavoriteButtonCtrl,
      controllerAs: 'vm',
      bindToController: true,
      replace: true,
      templateUrl: 'js/home/artifact-list/favorite-button.html'
    };
  }

  function FavoriteButtonCtrl() {
    var vm = this;

    vm.toggleFavorite = function() {
      if (vm.artifact.favorite) {
        vm.repository.unmarkAsFavorite(vm.artifact.id);
      } else {
        vm.repository.markAsFavorite(vm.artifact.id);
      }
      vm.artifact.favorite = !vm.artifact.favorite;
    };

    vm.isFavorite = function() {
      return vm.artifact.favorite;
    };
  }

})();
