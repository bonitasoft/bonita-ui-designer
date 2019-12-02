(function() {

  'use strict';

  class EditorHeaderCtrl {
    constructor(mode, artifact, artifactRepo, $uibModal, $stateParams, $state, $window, $localStorage, browserHistoryService, keyBindingService, $scope, $timeout, $q, artifactStore, artifactNamingValidatorService, dataManagementRepo) {
      'ngInject';
      this.mode = mode;
      this.page = artifact;
      this.artifactRepo = artifactRepo;
      this.$uibModal = $uibModal;
      this.$stateParams = $stateParams;
      this.$state = $state;
      this.$window = $window;
      this.$localStorage = $localStorage;
      this.$timeout = $timeout;
      this.$q = $q;
      this.browserHistoryService = browserHistoryService;
      this.pristine = true;
      this.dirty = false;
      this.scope = $scope;
      this.artifactNamingValidatorService = artifactNamingValidatorService;
      this.artifacts = [];

      // load all artifact with same type
      artifactStore.load().then((artifacts) => this.artifacts = artifacts.filter(item => item.type === this.page.type));

      keyBindingService.bindGlobal(['ctrl+s', 'command+s'], () => {
        $scope.$apply(() => this.save(this.page));
        // prevent default browser action
        return false;
      });

      $scope.$on('$destroy', function() {
        keyBindingService.unbind(['ctrl+s', 'command+s']);
      });

      dataManagementRepo.getDataObjects().then(data => {
        this.businessDataRepositoryOffline = data.error;
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
      if (!this.isPageDirty(page)) {
        let defer = this.$q.defer();
        defer.resolve();
        return defer.promise;
      }
      return this.artifactRepo.save(page)
        .then(response => {
          this.dirty = false;
          let location = response.headers('location');
          if (location) {
            return location.substring(location.lastIndexOf('/') + 1);
          } else {
            this.scope.$broadcast('saved');
          }
        })
        .then((newId) => {
          if (newId) {
            this.$stateParams.id = newId;
            this.$state.go(`designer.${page.type}`, this.$stateParams, { location: 'replace', reload: true });
            return newId;
          }
        });
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

    convert(page) {
      var modalInstance = this.$uibModal.open({
        templateUrl: 'js/editor/header/convert-popup.html',
        controller: 'ConvertPopUpController',
        controllerAs: 'ctrl',
        resolve: {
          page: () => page
        }
      });

      modalInstance.result
        .then(() => {
          this.save(page);
        });
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

    achieveSaveAndExport(page) {
      this.save(page)
        .then((newId) => {
          let exportPage = (page) => this.$window.location = this.artifactRepo.exportUrl(page);
          if (newId) {
            page.id = newId;
            //delay required in order for the new state to be applied
            this.$timeout(() => exportPage(page), 500);
          } else {
            exportPage(page);
          }
        });
    }

    saveAndExport(page) {
      let storage = this.$localStorage.bonitaUIDesigner;
      if (!storage || !storage.doNotShowExportMessageAgain) {
        var modalInstance = this.$uibModal.open({
          templateUrl: 'js/editor/header/export-popup.html',
          controller: 'ExportPopUpController',
          controllerAs: 'ctrl',
          resolve: {
            page: () => page
          }
        });

        modalInstance.result
          .then(() => {
            this.achieveSaveAndExport(page);
          });
      } else {
        this.achieveSaveAndExport(page);
      }
    }

    editMetadata(page) {
      var resources = this.artifactRepo.loadResources(page);
      var modalInstance = this.$uibModal.open({
        templateUrl: 'js/editor/header/metadata-popup.html',
        controller: 'MetadataPopUpController',
        controllerAs: 'ctrl',
        resolve: {
          page: () => page,
          resources: () => resources
        }
      });

      modalInstance.result
        .then(() => {
          this.save(page);
        });
    }

    isArtifactNameAlreadyExist(value, artifact) {
      if (value !== artifact.id) {
        return this.artifactNamingValidatorService.isArtifactNameAlreadyUseForType(value, artifact.type, this.artifacts);
      }
      return false;
    }
  }

  angular
    .module('bonitasoft.designer.editor.header')
    .controller('EditorHeaderCtrl', EditorHeaderCtrl);

})();
