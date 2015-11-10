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
      templateUrl: 'js/home/favorite-button.html'
    };
  }

  function FavoriteButtonCtrl(gettextCatalog) {
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

    vm.getTitle = function() {
      return vm.artifact.favorite ?
        gettextCatalog.getString('Unmark as favorite') :
        gettextCatalog.getString('Mark as favorite');
    };
  }

})();
