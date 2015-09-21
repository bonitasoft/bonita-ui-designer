(function () {

  'use strict';

  angular
    .module('bonitasoft.designer.editor.menu-bar')
    .controller('MenuBarCtrl', MenuBarCtrl);

  function MenuBarCtrl(mode, artifact, artifactRepo, $modal, $stateParams, $state, $window) {

    var vm = this;
    vm.mode = mode;
    vm.page = artifact;
    vm.save = save;
    vm.saveAs = saveAs;
    vm.saveAndExport = saveAndExport;
    vm.openHelp = openHelp;

    function save(page) {
      return artifactRepo.save(page.id, page);
    }

    function saveAs(page) {
      var modalInstance = $modal.open({
        templateUrl: 'js/editor/menu-bar/save-as-popup.html',
        controller: 'SaveAsPopUpController',
        resolve: {
          page: function () {
            return page;
          }
        }
      });

      modalInstance.result.then(saveDataAs).then(reload);

      function reload(data) {
        $stateParams.id = data.id;
        $state.go($state.current, $stateParams, {
          reload: true
        });
      }

      function saveDataAs(data){
        return artifactRepo.create(data, page.id);
      }
    }

    function saveAndExport(page) {
      artifactRepo.save(page.id, page)
        .then(function() {
          $window.location = artifactRepo.exportUrl(page);
        });
    }

    function openHelp() {
      $modal.open({
        templateUrl: 'js/editor/menu-bar/help-popup.html',
        size: 'lg',
        resolve: {
          pageEdition: function(){
            return 'page' === vm.mode;
          }
        },
        controller: function($scope, $modalInstance, pageEdition) {
          $scope.pageEdition = pageEdition;
          $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
          };
        }
      });
    }
  }

})();
