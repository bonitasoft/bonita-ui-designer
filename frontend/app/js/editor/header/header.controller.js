(function() {

  'use strict';

  class EditorHeaderCtrl {
    constructor(mode, artifact, artifactRepo, $uibModal, $stateParams, $state, $window) {
      'ngInject';
      this.mode = mode;
      this.page = artifact;
      this.artifactRepo = artifactRepo;
      this.$uibModal = $uibModal;
      this.$stateParams = $stateParams;
      this.$state = $state;
      this.$window = $window;
    }

    back() {
      this.$window.history.back();
    }

    save(page) {
      return this.artifactRepo.save(page);
    }

    saveAs(page) {
      var modalInstance = this.$uibModal.open({
        templateUrl: 'js/editor/header/save-as-popup.html',
        controller: 'SaveAsPopUpController',
        controllerAs: 'ctrl',
        resolve: {
          page: () => page
        }
      });

      modalInstance.result.then(data => this.artifactRepo.create(data, page.id)).then(data => {
        this.$stateParams.id = data.id;
        this.$state.go(`designer.${page.type}`, this.$stateParams, {
          reload: true
        });
      });
    }

    saveAndExport(page) {
      this.artifactRepo.save(page)
        .then(() =>this.$window.location = this.artifactRepo.exportUrl(page));
    }

    openHelp() {
      this.$uibModal.open({
        templateUrl: 'js/editor/header/help-popup.html',
        size: 'lg',
        resolve: {
          pageEdition: () => this.mode === 'page'
        },
        controller: function($scope, $modalInstance, pageEdition) {
          'ngInject';
          $scope.pageEdition = pageEdition;
          $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
          };
        }
      });
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('EditorHeaderCtrl', EditorHeaderCtrl);

})();
