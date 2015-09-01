(function () {

  angular
    .module('bonitasoft.designer.home')
    .controller('ImportArtifactSuccessMessageController', ImportArtifactSuccessMessageController);

  function ImportArtifactSuccessMessageController($scope, gettextCatalog) {

    var vm = this;

    vm.joinOnNames = joinOnNames;
    vm.getState = getState;

    function joinOnNames(artifacts) {
      return artifacts.map(function(item) {
        return item.name;
      }).join(', ');
    }

    function getState() {
      if ($scope.overridden) {
        return gettextCatalog.getString('overridden');
      }
      return gettextCatalog.getString('added');
    }
  }

})();
