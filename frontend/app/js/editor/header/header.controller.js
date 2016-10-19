(function() {

  'use strict';

  class EditorHeaderCtrl {
    constructor(mode, artifact, artifactRepo, $uibModal, $stateParams, $state, $window, browserHistoryService, keyBindingService, $scope) {
      'ngInject';
      this.mode = mode;
      this.page = artifact;
      this.artifactRepo = artifactRepo;
      this.$uibModal = $uibModal;
      this.$stateParams = $stateParams;
      this.$state = $state;
      this.$window = $window;
      this.browserHistoryService = browserHistoryService;
      this.pristine = true;
      this.dirty = false;
      this.scope = $scope;

      keyBindingService.bindGlobal(['ctrl+s', 'command+s'], () => {
        $scope.$apply(() => this.save(this.page));
        // prevent default browser action
        return false;
      });

      $scope.$on('$destroy', function() {
        keyBindingService.unbind(['ctrl+s', 'command+s']);
      });
    }

    back() {
      this.browserHistoryService.back(() => this.$state.go('designer.home'));
    }

    isPageDirty(artifact) {
      let needSave = this.artifactRepo.needSave(artifact);
      this.pristine = this.pristine && !needSave;
      this.dirty = this.dirty || needSave;
      return needSave;
    }

    save(page) {
      return this.artifactRepo.save(page)
        .then(() => this.dirty = false)
        .then(() => this.scope.$broadcast('saved'));
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

      modalInstance.result
        .then(data => this.removeReferences(data))
        .then(data => this.artifactRepo.create(data, page.id))
        .then(data => this.$stateParams.id = data.id)
        .then(() => this.$state.go(`designer.${page.type}`, this.$stateParams, { reload: true }));
    }

    removeReferences(data) {
      if (angular.isArray(data)) {
        data.forEach(item => this.removeReferences(item));
      } else if (angular.isObject(data)) {
        delete data.reference;
        angular.forEach(data, (value, key) => !key.match(/^\$/) && this.removeReferences(value));
      }
      return data;
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
        controller: function($scope, $uibModalInstance, pageEdition) {
          'ngInject';
          $scope.pageEdition = pageEdition;
          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };
        }
      });
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('EditorHeaderCtrl', EditorHeaderCtrl);

})();
