(function() {

  'use strict';

  let _artifactRepo, _$uibModal, _$stateParams, _$state, _$window;

  class MenuBarCtrl {
    constructor(mode, artifact, artifactRepo, $uibModal, $stateParams, $state, $window) {
      'ngInject';
      this.mode = mode;
      this.page = artifact;
      _artifactRepo = artifactRepo;
      _$uibModal = $uibModal;
      _$stateParams = $stateParams;
      _$state = $state;
      _$window = $window;
    }

    back() {
      _$window.history.back();
    }

    save(page) {
      return _artifactRepo.save(page);
    }

    saveAs(page) {
      var modalInstance = _$uibModal.open({
        templateUrl: 'js/editor/menu-bar/save-as-popup.html',
        controller: 'SaveAsPopUpController',
        controllerAs: 'ctrl',
        resolve: {
          page: function() {
            return page;
          }
        }
      });

      modalInstance.result.then(saveDataAs).then(reload);

      function reload(data) {
        _$stateParams.id = data.id;
        _$state.go(_$state.current, _$stateParams, {
          reload: true
        });
      }

      function saveDataAs(data) {
        return _artifactRepo.create(data, page.id);
      }
    }

    saveAndExport(page) {
      _artifactRepo.save(page)
        .then(function() {
          _$window.location = _artifactRepo.exportUrl(page);
        });
    }

    openHelp() {
      _$uibModal.open({
        templateUrl: 'js/editor/menu-bar/help-popup.html',
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
    .module('bonitasoft.designer.editor.menu-bar')
    .controller('MenuBarCtrl', MenuBarCtrl);

})();
